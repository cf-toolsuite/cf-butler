package org.cftoolsuite.cfapp.task;

import java.time.LocalDateTime;

import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.domain.ApplicationOperation;
import org.cftoolsuite.cfapp.domain.HistoricalRecord;
import org.cftoolsuite.cfapp.service.AppDetailService;
import org.cftoolsuite.cfapp.service.HistoricalRecordService;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.util.PolicyFilter;
import org.cloudfoundry.client.v3.applications.StopApplicationRequest;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class StopAppPolicyExecutorTask implements PolicyExecutorTask {

    private PolicyFilter filter;
    private DefaultCloudFoundryOperations opsClient;
    private AppDetailService appInfoService;
    private PoliciesService policiesService;
    private HistoricalRecordService historicalRecordService;

    @Autowired
    public StopAppPolicyExecutorTask(
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
    public void execute() {
        log.info("StopAppPolicyExecutorTask started");
        stopApplications()
        .collectList()
        .subscribe(
            result -> {
                log.info("StopAppPolicyExecutorTask completed");
                log.info("-- {} applications stopped.", result.size());
            },
            error -> {
                log.error("StopAppPolicyExecutorTask terminated with error", error);
            }
        );
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
        execute();
    }

    protected Mono<HistoricalRecord> stopApplication(AppDetail detail) {
        return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(detail.getOrganization())
                .space(detail.getSpace())
                .build()
                .getCloudFoundryClient()
                .applicationsV3()
                .stop(
                    StopApplicationRequest
                        .builder()
                        .applicationId(detail.getAppId())
                        .build()
                )
                .then(
                    Mono.just(
                        HistoricalRecord
                            .builder()
                            .transactionDateTime(LocalDateTime.now())
                            .actionTaken("stop")
                            .organization(detail.getOrganization())
                            .space(detail.getSpace())
                            .appId(detail.getAppId())
                            .type("application")
                            .name(detail.getAppName())
                            .build()
                    )
                );
    }

    protected Flux<HistoricalRecord> stopApplications() {
        return policiesService
                .findByApplicationOperation(ApplicationOperation.STOP)
                .flux()
                .flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
                .flatMap(ap -> Flux.concat(appInfoService.findByApplicationPolicy(ap, false), appInfoService.findByApplicationPolicy(ap, true)))
                .distinct()
                .filter(wl -> filter.isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
                .filter(bl -> filter.isBlacklisted(bl.getT1().getOrganization(), bl.getT1().getSpace()))
                .flatMap(ad -> stopApplication(ad.getT1()))
                .flatMap(historicalRecordService::save);
    }
}
