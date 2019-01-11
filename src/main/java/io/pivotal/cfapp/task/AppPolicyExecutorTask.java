package io.pivotal.cfapp.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cloudfoundry.client.v2.services.DeleteServiceRequest;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.DeleteApplicationRequest;
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
import io.pivotal.cfapp.domain.HistoricalRecord;
import io.pivotal.cfapp.service.AppInfoService;
import io.pivotal.cfapp.service.AppRelationshipService;
import io.pivotal.cfapp.service.HistoricalRecordService;
import io.pivotal.cfapp.service.PoliciesService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AppPolicyExecutorTask implements ApplicationRunner {

	private ButlerSettings settings;
	private DefaultCloudFoundryOperations opsClient;
    private AppInfoService appInfoService;
    private AppRelationshipService appRelationshipService;
    private PoliciesService policiesService;
    private HistoricalRecordService historicalRecordService;

    @Autowired
    public AppPolicyExecutorTask(
    		ButlerSettings settings,
    		DefaultCloudFoundryOperations opsClient,
    		AppInfoService appInfoService,
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
    	deleteApplicationsWithNoServiceBindings();
    	deleteApplicationsWithServiceBindingsButDoNotDeleteBoundServiceInstances();
    	deleteApplicationsWithServiceBindingsAndDeleteBoundServiceInstances();
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
    	execute();
    }

	protected void deleteApplicationsWithNoServiceBindings() {
    	// these are the applications with no service bindings
    	// we can delete each one without having to first unbind it from one or more service instances
    	policiesService
            .findAll()
            .flux()
            .flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
        	.flatMap(ap -> appInfoService.findByApplicationPolicy(ap, false))
        	.filter(bl -> !settings.getOrganizationBlackList().contains(bl.getOrganization()))
        	.flatMap(ad -> deleteApplication(ad))
            .flatMap(historicalRecordService::save)
            .subscribe();
    }
	
	protected void deleteApplicationsWithServiceBindingsButDoNotDeleteBoundServiceInstances() {
		// these are the applications with service bindings
		// in this case the application policy has been configured with delete-services = false
		// so we:  a) unbind one or more service instances from each application, b) delete each application
		policiesService
	        .findAll()
	        .flux()
	        .flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
			.filter(f -> f.isDeleteServices() == false)
			.flatMap(ap -> appInfoService.findByApplicationPolicy(ap, true))
			.filter(bl -> !settings.getOrganizationBlackList().contains(bl.getOrganization()))
			.flatMap(ar -> appRelationshipService.findByApplicationId(ar.getAppId()))
			.flatMap(ur -> unbindServiceInstance(ur))
			.distinct()
			.flatMap(a -> deleteApplication(a))
			.flatMap(historicalRecordService::save)
			.subscribe();
		
	}

	protected void deleteApplicationsWithServiceBindingsAndDeleteBoundServiceInstances() {
		// these are the applications with service bindings
		// in this case the application policy has been configured with delete-services = true
		// so we:  a) unbind one or more service instances from each application, b) delete each application, 
		// and c) delete each formerly bound service instance
		List<AppRelationship> appRelationships = new ArrayList<>();
		policiesService
	        .findAll()
	        .flux()
	        .flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
			.filter(f -> f.isDeleteServices() == true)
			.flatMap(ap -> appInfoService.findByApplicationPolicy(ap, true))
			.filter(bl -> !settings.getOrganizationBlackList().contains(bl.getOrganization()))
			.flatMap(ar -> appRelationshipService.findByApplicationId(ar.getAppId()))
			.subscribe(appRelationships::add);
			
		Flux.fromIterable(new ArrayList<>(appRelationships))
			.flatMap(ur -> unbindServiceInstance(ur))
			.distinct()
			.flatMap(a -> deleteApplication(a))
			.flatMap(historicalRecordService::save)
			.subscribe();
		
		Flux.fromIterable(new ArrayList<>(appRelationships))
			.flatMap(s -> deleteServiceInstance(s))
			.flatMap(historicalRecordService::save)
			.subscribe();
	}
    
	protected Mono<AppRequest> unbindServiceInstance(AppRelationship relationship) {
		DefaultCloudFoundryOperations.builder()
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
	            		.subscribe();
	     return Mono.just(AppRequest
	    		 			.builder()
	    		 				.id(relationship.getAppId())
	    		 				.organization(relationship.getOrganization())
	    		 				.space(relationship.getSpace())
	    		 				.appName(relationship.getAppName())
	    		 				.build());      
	}
	
    protected Mono<HistoricalRecord> deleteApplication(AppDetail detail) {
    	return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(detail.getOrganization())
                .space(detail.getSpace())
                .build()
				.applications()
					.delete(DeleteApplicationRequest.builder().name(detail.getAppName()).deleteRoutes(true).build())
					.map(r -> HistoricalRecord
								.builder()
									.dateTimeRemoved(LocalDateTime.now())
									.organization(detail.getOrganization())
									.space(detail.getSpace())
									.id(detail.getAppId())
									.type("application")
									.name(detail.getAppName())
									.build());			
    }
    
    protected Mono<HistoricalRecord> deleteApplication(AppRequest request) {
    	return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(request.getOrganization())
                .space(request.getSpace())
                .build()
				.applications()
					.delete(DeleteApplicationRequest.builder().name(request.getAppName()).deleteRoutes(true).build())
					.map(r -> HistoricalRecord
								.builder()
									.dateTimeRemoved(LocalDateTime.now())
									.organization(request.getOrganization())
									.space(request.getSpace())
									.id(request.getId())
									.type("application")
									.name(request.getAppName())
									.build());			
    }
    
    protected Mono<HistoricalRecord> deleteServiceInstance(AppRelationship relationship) {
    	return opsClient
			.getCloudFoundryClient()
				.services()
					.delete(DeleteServiceRequest.builder().serviceId(relationship.getServiceId()).purge(true).build())
					.map(r -> HistoricalRecord
								.builder()
									.dateTimeRemoved(LocalDateTime.now())
									.organization(relationship.getOrganization())
									.space(relationship.getSpace())
									.id(relationship.getServiceId())
									.type("service-instance")
									.name(String.join("::", relationship.getAppName(), relationship.getServiceType(), relationship.getServicePlan()))
									.status(r.getEntity().getStatus())
									.errorDetails(r.getEntity().getErrorDetails() != null ? r.getEntity().getErrorDetails().toString(): null)
									.build());			
    }
}
