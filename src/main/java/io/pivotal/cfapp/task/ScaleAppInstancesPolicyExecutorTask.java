package io.pivotal.cfapp.task;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.ScaleApplicationRequest;
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
public class ScaleAppInstancesPolicyExecutorTask implements PolicyExecutorTask {

	private PasSettings settings;
	private DefaultCloudFoundryOperations opsClient;
    private AppDetailService appInfoService;
    private PoliciesService policiesService;
    private HistoricalRecordService historicalRecordService;

    @Autowired
    public ScaleAppInstancesPolicyExecutorTask(
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
		log.info("ScaleAppInstancesPolicyExecutorTask started");
    	scaleApplications()
	    	.subscribe(
				result -> {
					log.info("ScaleAppInstancesPolicyExecutorTask completed");
					log.info("-- {} applications scaled.", result.size());
				},
				error -> {
					log.error("ScaleAppInstancesPolicyExecutorTask terminated with error", error);
				}
			);
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
    	execute();
    }

	protected Mono<List<HistoricalRecord>> scaleApplications() {
		return
			policiesService
				.findByApplicationOperation(ApplicationOperation.SCALE_INSTANCES)
					.flux()
					.flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
					.flatMap(ap -> Flux.concat(appInfoService.findByApplicationPolicy(ap, false), appInfoService.findByApplicationPolicy(ap, true)))
					.distinct()
					.filter(wl -> isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
					.filter(bl -> isBlacklisted(bl.getT1().getOrganization()))
					.filter(from -> from.getT1().getRunningInstances().equals(from.getT2().getOption("instances-from", Integer.class)))
					.flatMap(ad -> scaleApplication(ad.getT2(), ad.getT1()))
					.flatMap(historicalRecordService::save)
					.collectList();
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
									.build())
					.then(Mono.just(HistoricalRecord
								.builder()
									.transactionDateTime(LocalDateTime.now())
									.actionTaken("scale-instances")
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
