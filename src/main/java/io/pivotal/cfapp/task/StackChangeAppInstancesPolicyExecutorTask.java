package io.pivotal.cfapp.task;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cloudfoundry.client.v2.applications.RestageApplicationRequest;
import org.cloudfoundry.client.v2.applications.RestageApplicationResponse;
import org.cloudfoundry.client.v3.Lifecycle;
import org.cloudfoundry.client.v3.LifecycleData;
import org.cloudfoundry.client.v3.LifecycleType;
import org.cloudfoundry.client.v3.applications.UpdateApplicationRequest;
import org.cloudfoundry.client.v3.applications.UpdateApplicationResponse;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.ApplicationOperation;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.HistoricalRecord;
import io.pivotal.cfapp.service.AppDetailService;
import io.pivotal.cfapp.service.HistoricalRecordService;
import io.pivotal.cfapp.service.PoliciesService;
import io.pivotal.cfapp.service.StacksCache;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class StackChangeAppInstancesPolicyExecutorTask implements PolicyExecutorTask {

	private final ButlerSettings settings;
	private final DefaultCloudFoundryOperations opsClient;
    private final AppDetailService appInfoService;
	private final PoliciesService policiesService;
	private final StacksCache stacksCache;
    private final HistoricalRecordService historicalRecordService;

    @Autowired
    public StackChangeAppInstancesPolicyExecutorTask(
    		ButlerSettings settings,
    		DefaultCloudFoundryOperations opsClient,
    		AppDetailService appInfoService,
			PoliciesService policiesService,
			StacksCache stacksCache,
    		HistoricalRecordService historicalRecordService
    		) {
    	this.settings = settings;
        this.opsClient = opsClient;
        this.appInfoService = appInfoService;
		this.policiesService = policiesService;
		this.stacksCache = stacksCache;
        this.historicalRecordService = historicalRecordService;
    }

	@Override
    public void execute() {
		log.info("StackChangeAppInstancesPolicyExecutorTask started");
    	stackChangeApplications()
	    	.subscribe(
				result -> {
					log.info("StackChangeAppInstancesPolicyExecutorTask completed");
					log.info("-- {} applications updated.", result.size());
				},
				error -> {
					log.error("StackChangeAppInstancesPolicyExecutorTask terminated with error", error);
				}
			);
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
    	execute();
    }

	protected Mono<List<HistoricalRecord>> stackChangeApplications() {
		return
			Flux
				.concat(
					policiesService
						.findByApplicationOperation(ApplicationOperation.CHANGE_STACK)
							.flux()
							.flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
							.flatMap(ap -> appInfoService.findByApplicationPolicy(ap, false))
							.filter(wl -> isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
							.filter(bl -> isBlacklisted(bl.getT1().getOrganization()))
							.filter(from -> from.getT1().getStack().equals(from.getT2().getOption("stack-from", String.class)))
							.flatMap(ad -> stackChangeApplication(ad.getT2(), ad.getT1()))
							.flatMap(historicalRecordService::save),
					policiesService
						.findByApplicationOperation(ApplicationOperation.CHANGE_STACK)
							.flux()
							.flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
							.flatMap(ap -> appInfoService.findByApplicationPolicy(ap, true))
							.filter(wl -> isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
							.filter(bl -> isBlacklisted(bl.getT1().getOrganization()))
							.filter(from -> from.getT1().getStack().equals(from.getT2().getOption("stack-from", String.class)))
							.flatMap(ad -> stackChangeApplication(ad.getT2(), ad.getT1()))
							.flatMap(historicalRecordService::save)
				)
				.collectList();
    }

    protected Mono<HistoricalRecord> stackChangeApplication(ApplicationPolicy policy, AppDetail detail) {
    	return updateApplication(policy, detail)
				.flatMap(uar -> restageApplication(uar, detail))
				.then(Mono.just(HistoricalRecord
							.builder()
								.transactionDateTime(LocalDateTime.now())
								.actionTaken("change-stack")
								.organization(detail.getOrganization())
								.space(detail.getSpace())
								.appId(detail.getAppId())
								.type("application")
								.name(detail.getAppName())
								.build()));
    }

	private Mono<UpdateApplicationResponse> updateApplication(ApplicationPolicy policy, AppDetail detail) {
		LifecycleData data =
			BuildpackLifecycleData
				.builder()
				.stack(policy.getOption("stack-to", String.class))
				.build();
		Lifecycle lifecycle =
			Lifecycle
				.builder()
				.type(LifecycleType.BUILDPACK)
				.data(data)
				.build();
		return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(detail.getOrganization())
                .space(detail.getSpace())
                .build()
				.getCloudFoundryClient()
					.applicationsV3()
					.update(
							UpdateApplicationRequest
								.builder()
									.applicationId(detail.getAppId())
									.lifecycle(lifecycle)
									.build());
	}

	private Mono<RestageApplicationResponse> restageApplication(UpdateApplicationResponse response, AppDetail detail) {
		return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(detail.getOrganization())
                .space(detail.getSpace())
                .build()
				.getCloudFoundryClient()
					.applicationsV2()
					.restage(
							RestageApplicationRequest
								.builder()
									.applicationId(detail.getAppId())
									.build());
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

	@Builder
	@Getter
	static class BuildpackLifecycleData implements LifecycleData {

		private List<String> buildpacks;
		private String stack;
	}
}
