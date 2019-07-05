package io.pivotal.cfapp.task;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.services.DeleteServiceInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.HistoricalRecord;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.ServiceInstanceOperation;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import io.pivotal.cfapp.service.HistoricalRecordService;
import io.pivotal.cfapp.service.PoliciesService;
import io.pivotal.cfapp.service.ServiceInstanceDetailService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class DeleteServiceInstancePolicyExecutorTask implements PolicyExecutorTask {

	private ButlerSettings settings;
	private DefaultCloudFoundryOperations opsClient;
    private ServiceInstanceDetailService serviceInfoService;
    private PoliciesService policiesService;
    private HistoricalRecordService historicalRecordService;

    @Autowired
    public DeleteServiceInstancePolicyExecutorTask(
    		ButlerSettings settings,
    		DefaultCloudFoundryOperations opsClient,
    		ServiceInstanceDetailService serviceInfoService,
    		PoliciesService policiesService,
    		HistoricalRecordService historicalRecordService
    		) {
    	this.settings = settings;
        this.opsClient = opsClient;
        this.serviceInfoService = serviceInfoService;
        this.policiesService = policiesService;
        this.historicalRecordService = historicalRecordService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
    	// do nothing at startup
    }

	@Override
    public void execute() {
		log.info("DeleteServiceInstancePolicyExecutorTask started");
    	policiesService
	        .findByServiceInstanceOperation(ServiceInstanceOperation.DELETE)
				.flux()
				.flatMap(p -> Flux.fromIterable(p.getServiceInstancePolicies()))
				.flatMap(sp -> serviceInfoService.findByServiceInstancePolicy(sp))
				.filter(wl -> isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
				.filter(bl -> isBlacklisted(bl.getT1().getOrganization()))
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

    protected Mono<HistoricalRecord> deleteServiceInstance(ServiceInstanceDetail sd) {
    	return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(sd.getOrganization())
                .space(sd.getSpace())
                .build()
					.services()
						.deleteInstance(DeleteServiceInstanceRequest.builder().name(sd.getName()).build())
						.then(Mono.just(HistoricalRecord
									.builder()
										.transactionDateTime(LocalDateTime.now())
										.actionTaken("delete")
										.organization(sd.getOrganization())
										.space(sd.getSpace())
										.serviceInstanceId(sd.getServiceInstanceId())
										.type("service-instance")
										.name(String.join("__", sd.getName(), sd.getType(), sd.getPlan()))
										.build()));
    }

    private boolean isBlacklisted(String  organization) {
		return !settings.getOrganizationBlackList().contains(organization);
	}

    private boolean isWhitelisted(ServiceInstancePolicy policy, String organization) {
    	Set<String> prunedSet = new HashSet<>(policy.getOrganizationWhiteList());
    	while (prunedSet.remove(""));
    	Set<String> whitelist =
    			CollectionUtils.isEmpty(prunedSet) ?
    					prunedSet: policy.getOrganizationWhiteList();
    	return
			whitelist.isEmpty() ? true: policy.getOrganizationWhiteList().contains(organization);
	}
}
