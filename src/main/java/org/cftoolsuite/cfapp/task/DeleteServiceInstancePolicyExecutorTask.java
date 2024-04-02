package org.cftoolsuite.cfapp.task;

import java.time.LocalDateTime;

import org.cftoolsuite.cfapp.domain.HistoricalRecord;
import org.cftoolsuite.cfapp.domain.ServiceInstanceDetail;
import org.cftoolsuite.cfapp.domain.ServiceInstanceOperation;
import org.cftoolsuite.cfapp.service.HistoricalRecordService;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.service.ServiceInstanceDetailService;
import org.cftoolsuite.cfapp.util.PolicyFilter;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.services.DeleteServiceInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class DeleteServiceInstancePolicyExecutorTask implements PolicyExecutorTask {

    private PolicyFilter filter;
    private DefaultCloudFoundryOperations opsClient;
    private ServiceInstanceDetailService serviceInfoService;
    private PoliciesService policiesService;
    private HistoricalRecordService historicalRecordService;

    @Autowired
    public DeleteServiceInstancePolicyExecutorTask(
            PolicyFilter filter,
            DefaultCloudFoundryOperations opsClient,
            ServiceInstanceDetailService serviceInfoService,
            PoliciesService policiesService,
            HistoricalRecordService historicalRecordService
            ) {
        this.filter = filter;
        this.opsClient = opsClient;
        this.serviceInfoService = serviceInfoService;
        this.policiesService = policiesService;
        this.historicalRecordService = historicalRecordService;
    }

    protected Mono<HistoricalRecord> deleteServiceInstance(ServiceInstanceDetail sd) {
        return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(sd.getOrganization())
                .space(sd.getSpace())
                .build()
                .services()
                .deleteInstance(DeleteServiceInstanceRequest.builder().name(sd.getName()).build())
                .then(
                    Mono.just(
                        HistoricalRecord
                            .builder()
                            .transactionDateTime(LocalDateTime.now())
                            .actionTaken("delete")
                            .organization(sd.getOrganization())
                            .space(sd.getSpace())
                            .serviceInstanceId(sd.getServiceInstanceId())
                            .type("service-instance")
                            .name(String.join("__", sd.getName(), sd.getType(), sd.getPlan()))
                            .build()
                    )
                );
    }

    @Override
    public void execute() {
        log.info("DeleteServiceInstancePolicyExecutorTask started");
        policiesService
            .findByServiceInstanceOperation(ServiceInstanceOperation.DELETE)
            .flux()
            .flatMap(p -> Flux.fromIterable(p.getServiceInstancePolicies()))
            .flatMap(sp -> serviceInfoService.findByServiceInstancePolicy(sp))
            .filter(wl -> filter.isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
            .filter(bl -> filter.isBlacklisted(bl.getT1().getOrganization(), bl.getT1().getSpace()))
            .flatMap(ds -> deleteServiceInstance(ds.getT1()))
            .flatMap(historicalRecordService::save)
            .collectList()
            .subscribe(
                result -> {
                    log.info("DeleteServiceInstancePolicyExecutorTask completed");
                    log.info("-- {} service instances deleted.", result.size());
                },
                error -> {
                    log.error("DeleteServiceInstancePolicyExecutorTask terminated with error", error);
                }
            );
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
        execute();
    }
}
