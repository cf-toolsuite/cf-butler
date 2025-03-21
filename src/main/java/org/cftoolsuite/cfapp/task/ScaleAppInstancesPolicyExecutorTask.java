package org.cftoolsuite.cfapp.task;

import java.time.LocalDateTime;
import java.util.List;

import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.domain.ApplicationOperation;
import org.cftoolsuite.cfapp.domain.ApplicationPolicy;
import org.cftoolsuite.cfapp.domain.HistoricalRecord;
import org.cftoolsuite.cfapp.service.AppDetailService;
import org.cftoolsuite.cfapp.service.HistoricalRecordService;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.util.PolicyFilter;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.ScaleApplicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ScaleAppInstancesPolicyExecutorTask implements PolicyExecutorTask {

    private final PolicyFilter filter;
    private final DefaultCloudFoundryOperations opsClient;
    private final AppDetailService appInfoService;
    private final PoliciesService policiesService;
    private final HistoricalRecordService historicalRecordService;

    @Autowired
    public ScaleAppInstancesPolicyExecutorTask(
        PolicyFilter filter,
            DefaultCloudFoundryOperations opsClient,
            AppDetailService appInfoService,
            PoliciesService policiesService,
            HistoricalRecordService historicalRecordService
            ) {
        this.filter = filter;
        this.opsClient = opsClient;
        this.appInfoService = appInfoService;
        this.policiesService = policiesService;
        this.historicalRecordService = historicalRecordService;
    }

    @Override
    public void execute(String id) {
        log.info("ScaleAppInstancesPolicyExecutorTask with id={} started", id);
        scaleApplications(id)
            .subscribe(
                result -> {
                    log.info("ScaleAppInstancesPolicyExecutorTask with id={} completed", id);
                    log.info("-- {} applications scaled.", result.size());
                },
                error -> {
                    log.error(String.format("ScaleAppInstancesPolicyExecutorTask with id=%s terminated with error", id), error);
                }
            );
    }

    protected Mono<HistoricalRecord> scaleApplication(ApplicationPolicy policy, AppDetail detail) {
        return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(detail.getOrganization())
                .space(detail.getSpace())
                .build()
                .applications()
                .scale(
                    ScaleApplicationRequest
                        .builder()
                        .name(detail.getAppName())
                        .instances(policy.getOption("instances-to", Integer.class))
                        .build()
                )
                .then(
                    Mono.just(
                        HistoricalRecord
                            .builder()
                            .transactionDateTime(LocalDateTime.now())
                            .actionTaken("scale-instances")
                            .organization(detail.getOrganization())
                            .space(detail.getSpace())
                            .appId(detail.getAppId())
                            .type("application")
                            .name(detail.getAppName())
                            .build()
                    )
                );
    }

    protected Mono<List<HistoricalRecord>> scaleApplications(String id) {
        return
            policiesService
                .findByApplicationOperation(ApplicationOperation.SCALE_INSTANCES)
                .flux()
                .flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
                .filter(ap -> ap.getId().equals(id))
                .flatMap(ap -> Flux.concat(appInfoService.findByApplicationPolicy(ap, false), appInfoService.findByApplicationPolicy(ap, true)))
                .distinct()
                .filter(wl -> filter.isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
                .filter(bl -> filter.isBlacklisted(bl.getT1().getOrganization(), bl.getT1().getSpace()))
                .filter(from -> from.getT1().getRunningInstances() >= from.getT2().getOption("instances-from", Integer.class))
                .flatMap(ad -> scaleApplication(ad.getT2(), ad.getT1()))
                .flatMap(historicalRecordService::save)
                .collectList();
    }
}
