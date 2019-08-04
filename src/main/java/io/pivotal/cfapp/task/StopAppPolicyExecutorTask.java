package io.pivotal.cfapp.task;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.cloudfoundry.client.v3.applications.StopApplicationRequest;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.ApplicationOperation;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.HistoricalRecord;
import io.pivotal.cfapp.service.AppDetailService;
import io.pivotal.cfapp.service.HistoricalRecordService;
import io.pivotal.cfapp.service.PoliciesService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class StopAppPolicyExecutorTask implements PolicyExecutorTask {

	private PasSettings settings;
	private DefaultCloudFoundryOperations opsClient;
    private AppDetailService appInfoService;
    private PoliciesService policiesService;
    private HistoricalRecordService historicalRecordService;

    @Autowired
    public StopAppPolicyExecutorTask(
    		PasSettings settings,
    		DefaultCloudFoundryOperations opsClient,
    		AppDetailService appInfoService,
    		PoliciesService policiesService,
    		HistoricalRecordService historicalRecordService
    		) {
    	this.settings = settings;
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

	protected Flux<HistoricalRecord> stopApplications() {
    	return policiesService
					.findByApplicationOperation(ApplicationOperation.STOP)
			            .flux()
			            .flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
			        	.flatMap(ap -> Flux.concat(appInfoService.findByApplicationPolicy(ap, false), appInfoService.findByApplicationPolicy(ap, true)))
						.distinct()
						.filter(wl -> isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
			        	.filter(bl -> isBlacklisted(bl.getT1().getOrganization()))
			        	.flatMap(ad -> stopApplication(ad.getT1()))
						.flatMap(historicalRecordService::save);
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
									.build())
					.then(Mono.just(HistoricalRecord
								.builder()
									.transactionDateTime(LocalDateTime.now())
									.actionTaken("stop")
									.organization(detail.getOrganization())
									.space(detail.getSpace())
									.appId(detail.getAppId())
									.type("application")
									.name(detail.getAppName())
									.build()));
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
