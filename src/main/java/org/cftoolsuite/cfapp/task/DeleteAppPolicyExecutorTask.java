package org.cftoolsuite.cfapp.task;

import java.time.LocalDateTime;

import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.domain.AppRelationship;
import org.cftoolsuite.cfapp.domain.ApplicationOperation;
import org.cftoolsuite.cfapp.domain.HistoricalRecord;
import org.cftoolsuite.cfapp.service.AppDetailService;
import org.cftoolsuite.cfapp.service.AppRelationshipService;
import org.cftoolsuite.cfapp.service.HistoricalRecordService;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.util.PolicyFilter;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.DeleteApplicationRequest;
import org.cloudfoundry.operations.services.DeleteServiceInstanceRequest;
import org.cloudfoundry.operations.services.UnbindServiceInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class DeleteAppPolicyExecutorTask implements PolicyExecutorTask {

    private PolicyFilter filter;
    private DefaultCloudFoundryOperations opsClient;
    private AppDetailService appInfoService;
    private AppRelationshipService appRelationshipService;
    private PoliciesService policiesService;
    private HistoricalRecordService historicalRecordService;

    @Autowired
    public DeleteAppPolicyExecutorTask(
            PolicyFilter filter,
            DefaultCloudFoundryOperations opsClient,
            AppDetailService appInfoService,
            AppRelationshipService appRelationshipService,
            PoliciesService policiesService,
            HistoricalRecordService historicalRecordService
            ) {
        this.filter = filter;
        this.opsClient = opsClient;
        this.appInfoService = appInfoService;
        this.appRelationshipService = appRelationshipService;
        this.policiesService = policiesService;
        this.historicalRecordService = historicalRecordService;
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
                        .build()
                )
                .then(
                    Mono.just(
                        HistoricalRecord
                            .builder()
                            .transactionDateTime(LocalDateTime.now())
                            .actionTaken("delete")
                            .organization(detail.getOrganization())
                            .space(detail.getSpace())
                            .appId(detail.getAppId())
                            .type("application")
                            .name(detail.getAppName())
                            .build()
                    )
                );
    }

    protected Flux<HistoricalRecord> deleteApplicationsWithNoServiceBindings() {
        // these are the applications with no service bindings
        // we can delete each one without having to first unbind it from one or more service instances
        return policiesService
                .findByApplicationOperation(ApplicationOperation.DELETE)
                .flux()
                .flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
                .flatMap(ap -> appInfoService.findByApplicationPolicy(ap, false))
                .filter(wl -> filter.isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
                .filter(bl -> filter.isBlacklisted(bl.getT1().getOrganization(), bl.getT1().getSpace()))
                .flatMap(ad -> deleteApplication(ad.getT1()))
                .flatMap(historicalRecordService::save);
    }

    protected Flux<HistoricalRecord> deleteApplicationsWithServiceBindingsAndDeleteBoundServiceInstances() {
        // these are the applications with service bindings
        // in this case the application policy has been configured with delete-services = true
        // so we:  a) unbind one or more service instances from each application, b) delete each application,
        // and c) delete each formerly bound service instance
        return policiesService
                .findByApplicationOperation(ApplicationOperation.DELETE)
                .flux()
                .flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
                .filter(f -> f.getOption("delete-services", Boolean.class) == true)
                .flatMap(ap -> appInfoService.findByApplicationPolicy(ap, true))
                .filter(wl -> filter.isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
                .filter(bl -> filter.isBlacklisted(bl.getT1().getOrganization(), bl.getT1().getSpace()))
                .flatMap(ar -> appRelationshipService.findByApplicationId(ar.getT1().getAppId()))
                .flatMap(this::unbindServiceInstance)
                .flatMap(historicalRecordService::save)
                .flatMap(ad -> appInfoService.findByAppId(ad.getAppId()))
                .distinct()
                .flatMap(this::deleteApplication)
                .flatMap(historicalRecordService::save)
                .flatMap(dad -> appRelationshipService.findByApplicationId(dad.getAppId()))
                .flatMap(this::deleteServiceInstance)
                .flatMap(historicalRecordService::save);
    }

    protected Flux<HistoricalRecord> deleteApplicationsWithServiceBindingsButDoNotDeleteBoundServiceInstances() {
        // these are the applications with service bindings
        // in this case the application policy has been configured with delete-services = false
        // so we:  a) unbind one or more service instances from each application, b) delete each application
        return policiesService
                .findByApplicationOperation(ApplicationOperation.DELETE)
                .flux()
                .flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
                .filter(f -> f.getOption("delete-services", Boolean.class) == false)
                .flatMap(ap -> appInfoService.findByApplicationPolicy(ap, true))
                .filter(wl -> filter.isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
                .filter(bl -> filter.isBlacklisted(bl.getT1().getOrganization(), bl.getT1().getSpace()))
                .flatMap(ar -> appRelationshipService.findByApplicationId(ar.getT1().getAppId()))
                .flatMap(this::unbindServiceInstance)
                .flatMap(historicalRecordService::save)
                .flatMap(ad -> appInfoService.findByAppId(ad.getAppId()))
                .distinct()
                .flatMap(this::deleteApplication)
                .flatMap(historicalRecordService::save);
    }

    protected Mono<HistoricalRecord> deleteServiceInstance(AppRelationship relationship) {
        return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(relationship.getOrganization())
                .space(relationship.getSpace())
                .build()
                .services()
                .deleteInstance(DeleteServiceInstanceRequest.builder().name(relationship.getServiceName()).build())
                .then(
                    Mono.just(
                        HistoricalRecord
                            .builder()
                            .transactionDateTime(LocalDateTime.now())
                            .actionTaken("delete")
                            .organization(relationship.getOrganization())
                            .space(relationship.getSpace())
                            .appId(relationship.getAppId())
                            .serviceInstanceId(relationship.getServiceInstanceId())
                            .type("service-instance")
                            .name(serviceInstanceName(relationship))
                            .build()
                    )
                );
    }

    @Override
    public void execute() {
        log.info("DeleteAppPolicyExecutorTask started");
        Flux.concat(
            deleteApplicationsWithNoServiceBindings(),
            deleteApplicationsWithServiceBindingsButDoNotDeleteBoundServiceInstances(),
            deleteApplicationsWithServiceBindingsAndDeleteBoundServiceInstances()
        )
        .collectList()
        .subscribe(
            result -> {
                log.info("DeleteAppPolicyExecutorTask completed");
                log.info("-- {} applications deleted.", result.size());
            },
            error -> {
                log.error("DeleteAppPolicyExecutorTask terminated with error", error);
            }
        );
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
        execute();
    }

    private String serviceInstanceName(AppRelationship relationship) {
        return String.join("__", relationship.getServiceName(), relationship.getServiceType(), relationship.getServicePlan());
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
                        .build()
                )
                .then(
                    Mono.just(
                        HistoricalRecord
                            .builder()
                            .transactionDateTime(LocalDateTime.now())
                            .actionTaken("unbind")
                            .organization(relationship.getOrganization())
                            .space(relationship.getSpace())
                            .appId(relationship.getAppId())
                            .serviceInstanceId(relationship.getServiceInstanceId())
                            .type("service-instance")
                            .name(serviceInstanceName(relationship))
                            .build()
                    )
                );
    }
}
