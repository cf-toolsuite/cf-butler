package io.pivotal.cfapp.task;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.DeleteApplicationRequest;
import org.cloudfoundry.operations.services.DeleteServiceInstanceRequest;
import org.cloudfoundry.operations.services.UnbindServiceInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppRelationship;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.HistoricalRecord;
import io.pivotal.cfapp.service.AppDetailService;
import io.pivotal.cfapp.service.AppRelationshipService;
import io.pivotal.cfapp.service.HistoricalRecordService;
import io.pivotal.cfapp.service.PoliciesService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
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
		log.info("AppPolicyExecutorTask started");
    	deleteApplicationsWithNoServiceBindings()
	    	.then(deleteApplicationsWithServiceBindingsButDoNotDeleteBoundServiceInstances())
	    	.then(deleteApplicationsWithServiceBindingsAndDeleteBoundServiceInstances())
	    	.subscribe(
				result -> {
					log.info("AppPolicyExecutorTask completed");
				},
				error -> {
					log.error("AppPolicyExecutorTask terminated with error", error);
				}
			);
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
						.filter(wl -> isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
			        	.filter(bl -> isBlacklisted(bl.getT1().getOrganization()))
			        	.flatMap(ad -> deleteApplication(ad.getT1()))
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
						.filter(wl -> isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
			        	.filter(bl -> isBlacklisted(bl.getT1().getOrganization()))
						.flatMap(ar -> appRelationshipService.findByApplicationId(ar.getT1().getAppId()))
						.flatMap(this::unbindServiceInstance)
						.flatMap(historicalRecordService::save)
						.flatMap(ad -> appInfoService.findByAppId(ad.getAppId()))
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
		                .filter(wl -> isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
			        	.filter(bl -> isBlacklisted(bl.getT1().getOrganization()))
						.flatMap(ar -> appRelationshipService.findByApplicationId(ar.getT1().getAppId()))
			            .flatMap(this::unbindServiceInstance)
						.flatMap(historicalRecordService::save)
						.flatMap(ad -> appInfoService.findByAppId(ad.getAppId()))
						.distinct()
						.flatMap(this::deleteApplication)
						.flatMap(historicalRecordService::save)
			            .flatMap(dad -> appRelationshipService.findByApplicationId(dad.getAppId()))
			            .flatMap(this::deleteServiceInstance)
						.flatMap(historicalRecordService::save)
						.then();
	}

	protected Mono<HistoricalRecord> unbindServiceInstance(AppRelationship relationship) {
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
	            		.then(Mono.just(HistoricalRecord
									.builder()
										.transactionDateTime(LocalDateTime.now())
										.actionTaken("unbind")
										.organization(relationship.getOrganization())
										.space(relationship.getSpace())
										.appId(relationship.getAppId())
										.serviceInstanceId(relationship.getServiceInstanceId())
										.type("service-instance")
										.name(serviceInstanceName(relationship))
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
					.then(Mono.just(HistoricalRecord
								.builder()
									.transactionDateTime(LocalDateTime.now())
									.actionTaken("delete")
									.organization(detail.getOrganization())
									.space(detail.getSpace())
									.appId(detail.getAppId())
									.type("application")
									.name(detail.getAppName())
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
						.then(Mono.just(HistoricalRecord
									.builder()
										.transactionDateTime(LocalDateTime.now())
										.actionTaken("delete")
										.organization(relationship.getOrganization())
										.space(relationship.getSpace())
										.appId(relationship.getAppId())
										.serviceInstanceId(relationship.getServiceInstanceId())
										.type("service-instance")
										.name(serviceInstanceName(relationship))
										.build()));
    }

	private String serviceInstanceName(AppRelationship relationship) {
		return String.join("__", relationship.getServiceName(), relationship.getServiceType(), relationship.getServicePlan());
	}

    private boolean isBlacklisted(String  organization) {
		return !settings.getOrganizationBlackList().contains(organization);
	}

    private boolean isWhitelisted(ApplicationPolicy policy, String organization) {
    	Set<String> prunedSet = new HashSet<>(policy.getOrganizationWhiteList());
    	while (prunedSet.remove(""));
    	Set<String> whitelist =
    			CollectionUtils.isEmpty(prunedSet) ?
    					prunedSet: policy.getOrganizationWhiteList();
    	return
			whitelist.isEmpty() ? true: policy.getOrganizationWhiteList().contains(organization);
	}
}
