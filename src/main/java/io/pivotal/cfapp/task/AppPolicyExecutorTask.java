package io.pivotal.cfapp.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.function.Predicate;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.DeleteApplicationRequest;
import org.cloudfoundry.operations.services.DeleteServiceInstanceRequest;
import org.cloudfoundry.operations.services.UnbindServiceInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppRelationship;
import io.pivotal.cfapp.domain.AppRequest;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.HistoricalRecord;
import io.pivotal.cfapp.service.AppDetailService;
import io.pivotal.cfapp.service.AppRelationshipService;
import io.pivotal.cfapp.service.HistoricalRecordService;
import io.pivotal.cfapp.service.PoliciesService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
public class AppPolicyExecutorTask implements ApplicationRunner {

	private ButlerSettings settings;
	private DefaultCloudFoundryOperations opsClient;
    private AppDetailService appInfoService;
    private AppRelationshipService appRelationshipService;
    private PoliciesService policiesService;
    private HistoricalRecordService historicalRecordService;

    @Autowired
    public AppPolicyExecutorTask(
    		ButlerSettings settings,
    		DefaultCloudFoundryOperations opsClient,
    		AppDetailService appInfoService,
    		AppRelationshipService appRelationshipService,
    		PoliciesService policiesService,
    		HistoricalRecordService historicalRecordService
    		) {
    	this.settings = settings;
        this.opsClient = opsClient;
        this.appInfoService = appInfoService;
        this.appRelationshipService = appRelationshipService;
        this.policiesService = policiesService;
        this.historicalRecordService = historicalRecordService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
    	// do nothing at startup
    }
    
    public void execute() {
    	// TODO implement filter based on each policy's organization-whitelist
    	Hooks.onOperatorDebug();
    	deleteApplicationsWithNoServiceBindings()
	    	.then(deleteApplicationsWithServiceBindingsButDoNotDeleteBoundServiceInstances())
	    	.then(deleteApplicationsWithServiceBindingsAndDeleteBoundServiceInstances())
	    	.subscribe();
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
    	execute();
    }

	protected Mono<Void> deleteApplicationsWithNoServiceBindings() {
    	// these are the applications with no service bindings
    	// we can delete each one without having to first unbind it from one or more service instances
    	return policiesService
		            .findAll()
			            .flux()
			            .flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
			        	.flatMap(ap -> appInfoService.findByApplicationPolicy(ap, false))
						.filter(isWhitelisted())
						.map(ad -> ad.getT1())
			        	.filter(isBlacklisted())
			        	.flatMap(this::deleteApplication)
			            .flatMap(historicalRecordService::save)
			            .then();
    }
	
	protected Mono<Void> deleteApplicationsWithServiceBindingsButDoNotDeleteBoundServiceInstances() {
		// these are the applications with service bindings
		// in this case the application policy has been configured with delete-services = false
		// so we:  a) unbind one or more service instances from each application, b) delete each application
		return policiesService
			        .findAll()
				        .flux()
				        .flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
						.filter(f -> f.isDeleteServices() == false)
						.flatMap(ap -> appInfoService.findByApplicationPolicy(ap, true))
						.filter(isWhitelisted())
						.map(ad -> ad.getT1())
						.filter(isBlacklisted())
						.flatMap(ar -> appRelationshipService.findByApplicationId(ar.getAppId()))
						.flatMap(this::unbindServiceInstance)
						.distinct()
						.flatMap(this::deleteApplication)
						.flatMap(historicalRecordService::save)
						.then();
	}

	protected Mono<Void> deleteApplicationsWithServiceBindingsAndDeleteBoundServiceInstances() {
		// these are the applications with service bindings
		// in this case the application policy has been configured with delete-services = true
		// so we:  a) unbind one or more service instances from each application, b) delete each application, 
		// and c) delete each formerly bound service instance
		return policiesService
                .findAll()
                .flux()
                .flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
                .filter(f -> f.isDeleteServices() == true)
                .flatMap(ap -> appInfoService.findByApplicationPolicy(ap, true))
                .filter(isWhitelisted())
				.map(ad -> ad.getT1())
                .filter(isBlacklisted())
                .flatMap(ar -> appRelationshipService.findByApplicationId(ar.getAppId()))
                .collectList()
                .flatMap(appRelationships -> 
                	Flux.fromIterable(new ArrayList<>(appRelationships))
	                         .flatMap(this::unbindServiceInstance)
	                         .distinct()
	                         .flatMap(this::deleteApplication)
	                         .flatMap(historicalRecordService::save)
	                         .then(Flux.fromIterable(new ArrayList<>(appRelationships))
	                                   .flatMap(this::deleteServiceInstance)
	                                   .flatMap(historicalRecordService::save)
	                                   .then()
	                         ));
	}
    
	protected Mono<AppRequest> unbindServiceInstance(AppRelationship relationship) {
		return DefaultCloudFoundryOperations.builder()
	            .from(opsClient)
	            .organization(relationship.getOrganization())
	            .space(relationship.getSpace())
	            .build()
	            	.services()
	            		.unbind(
	            				UnbindServiceInstanceRequest
	            					.builder()
	            						.applicationName(relationship.getAppName())
	            						.serviceInstanceName(relationship.getServiceName())
	            						.build())
	            		.then(
	            				Mono.just(AppRequest
			    		 			.builder()
			    		 				.id(relationship.getAppId())
			    		 				.organization(relationship.getOrganization())
			    		 				.space(relationship.getSpace())
			    		 				.appName(relationship.getAppName())
			    		 				.build()));      
	}
	
    protected Mono<HistoricalRecord> deleteApplication(AppDetail detail) {
    	return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(detail.getOrganization())
                .space(detail.getSpace())
                .build()
				.applications()
					.delete(
							DeleteApplicationRequest
								.builder()
									.name(detail.getAppName())
									.deleteRoutes(true)
									.build())
					.then(
							Mono.just(HistoricalRecord
										.builder()
											.dateTimeRemoved(LocalDateTime.now())
											.organization(detail.getOrganization())
											.space(detail.getSpace())
											.id(detail.getAppId())
											.type("application")
											.name(detail.getAppName())
											.build()));
    }
    
    protected Mono<HistoricalRecord> deleteApplication(AppRequest request) {
    	return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(request.getOrganization())
                .space(request.getSpace())
                .build()
				.applications()
					.delete(
							DeleteApplicationRequest
								.builder()
									.name(request.getAppName())
									.deleteRoutes(true)
									.build())
					.then(
							Mono.just(HistoricalRecord
										.builder()
											.dateTimeRemoved(LocalDateTime.now())
											.organization(request.getOrganization())
											.space(request.getSpace())
											.id(request.getId())
											.type("application")
											.name(request.getAppName())
											.build()));
    }
    
    protected Mono<HistoricalRecord> deleteServiceInstance(AppRelationship relationship) {
    	return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(relationship.getOrganization())
                .space(relationship.getSpace())
                .build()
					.services()
						.deleteInstance(DeleteServiceInstanceRequest.builder().name(relationship.getServiceName()).build())
						.map(r -> HistoricalRecord
									.builder()
										.dateTimeRemoved(LocalDateTime.now())
										.organization(relationship.getOrganization())
										.space(relationship.getSpace())
										.id(relationship.getServiceId())
										.type("service-instance")
										.name(String.join("::", relationship.getServiceName(), relationship.getServiceType(), relationship.getServicePlan()))
										.build());			
    }
    
    private Predicate<? super AppDetail> isBlacklisted() {
		return bl -> !settings.getOrganizationBlackList().contains(bl.getOrganization());
	}
    
    private Predicate<? super Tuple2<AppDetail, ApplicationPolicy>> isWhitelisted() {
		return wl -> wl.getT2().whiteListExists() ? 
			wl.getT2().getOrganizationWhiteList().contains(wl.getT1().getOrganization()): true;
	}
}
