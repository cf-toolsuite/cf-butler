package io.pivotal.cfapp.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.cloudfoundry.client.v3.Lifecycle;
import org.cloudfoundry.client.v3.LifecycleData;
import org.cloudfoundry.client.v3.LifecycleType;
import org.cloudfoundry.client.v3.Relationship;
import org.cloudfoundry.client.v3.applications.SetApplicationCurrentDropletRequest;
import org.cloudfoundry.client.v3.applications.SetApplicationCurrentDropletResponse;
import org.cloudfoundry.client.v3.applications.UpdateApplicationRequest;
import org.cloudfoundry.client.v3.applications.UpdateApplicationResponse;
import org.cloudfoundry.client.v3.builds.BuildState;
import org.cloudfoundry.client.v3.builds.CreateBuildRequest;
import org.cloudfoundry.client.v3.builds.CreateBuildResponse;
import org.cloudfoundry.client.v3.builds.GetBuildRequest;
import org.cloudfoundry.client.v3.builds.GetBuildResponse;
import org.cloudfoundry.client.v3.packages.ListPackagesRequest;
import org.cloudfoundry.client.v3.packages.PackageResource;
import org.cloudfoundry.client.v3.packages.PackageState;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.RestartApplicationRequest;
import org.cloudfoundry.util.DelayTimeoutException;
import org.cloudfoundry.util.DelayUtils;
import org.cloudfoundry.util.ExceptionUtils;
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
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class StackChangeAppInstancesPolicyExecutorTask implements PolicyExecutorTask {

	private final PasSettings settings;
	private final DefaultCloudFoundryOperations opsClient;
    private final AppDetailService appInfoService;
	private final PoliciesService policiesService;
    private final HistoricalRecordService historicalRecordService;

    @Autowired
    public StackChangeAppInstancesPolicyExecutorTask(
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

	// FIXME current implementation has no recoverability and is not capable of zero-downtime deployment
	protected Mono<List<HistoricalRecord>> stackChangeApplications() {
		return
			policiesService
				.findByApplicationOperation(ApplicationOperation.CHANGE_STACK)
					.flux()
					.flatMap(p -> Flux.fromIterable(p.getApplicationPolicies()))
					.flatMap(ap -> Flux.concat(appInfoService.findByApplicationPolicy(ap, false), appInfoService.findByApplicationPolicy(ap, true)))
					.distinct()
					.filter(wl -> isWhitelisted(wl.getT2(), wl.getT1().getOrganization()))
					.filter(bl -> isBlacklisted(bl.getT1().getOrganization()))
					.filter(from -> from.getT1().getStack().equals(from.getT2().getOption("stack-from", String.class)))
					.flatMap(ad -> {
						log.info("{} is a candidate for stack change using policy {}.", ad.getT1(), ad.getT2());
						return stackChangeApplication(ad.getT2(), ad.getT1());
					})
					.flatMap(historicalRecordService::save)
					.collectList();
    }

    protected Mono<HistoricalRecord> stackChangeApplication(ApplicationPolicy policy, AppDetail detail) {
		return assignTargetStack(policy, detail)
				.flatMap(updatedApp -> getPackage(updatedApp, detail))
				.flatMap(pkg -> createBuild(pkg, detail))
				.flatMap(build -> waitForStagedBuild(build, detail, null))
				.flatMap(build -> setDroplet(build, detail))
				.flatMap(build -> restartApp(detail))
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

	private Mono<UpdateApplicationResponse> assignTargetStack(ApplicationPolicy policy, AppDetail detail) {
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

	private Mono<PackageResource> getPackage(UpdateApplicationResponse updatedApplication, AppDetail detail) {
		log.info("Attempting to fetch package from {}", updatedApplication.getLinks().get("packages").getHref());
		return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(detail.getOrganization())
                .space(detail.getSpace())
                .build()
				.getCloudFoundryClient()
					.packages()
						.list(ListPackagesRequest.builder().applicationId(detail.getAppId()).build())
						.flatMapMany(response -> Flux.fromIterable(response.getResources()))
						.filter(resource -> resource.getState().equals(PackageState.READY))
						.next();
	}

	private Mono<CreateBuildResponse> createBuild(PackageResource packageResource, AppDetail detail) {
		log.info("Attempting to create build with package {}", packageResource);
		Relationship pkg = Relationship.builder().id(packageResource.getId()).build();
		return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(detail.getOrganization())
                .space(detail.getSpace())
                .build()
				.getCloudFoundryClient()
					.builds()
						.create(CreateBuildRequest.builder().getPackage(pkg).build());
	}

	private Mono<GetBuildResponse> waitForStagedBuild(CreateBuildResponse build, AppDetail detail, Duration stagingTimeout) {
        Duration timeout = Optional.ofNullable(stagingTimeout).orElse(Duration.ofMinutes(15));
        return getBuild(build, detail)
            .filter(isStagingComplete())
            .repeatWhenEmpty(DelayUtils.exponentialBackOff(Duration.ofSeconds(1), Duration.ofSeconds(15), timeout))
            .filter(isStaged())
            .switchIfEmpty(ExceptionUtils.illegalState("Build %s failed during staging", build.getId()))
            .onErrorResume(DelayTimeoutException.class, t -> ExceptionUtils.illegalState("Build %s timed out during staging", build.getId()));
	}

	private Mono<GetBuildResponse> getBuild(CreateBuildResponse build, AppDetail detail) {
		return DefaultCloudFoundryOperations.builder()
				.from(opsClient)
				.organization(detail.getOrganization())
				.space(detail.getSpace())
				.build()
					.getCloudFoundryClient()
					.builds()
						.get(GetBuildRequest.builder().buildId(build.getId()).build());
	}

	private Mono<SetApplicationCurrentDropletResponse> setDroplet(GetBuildResponse build, AppDetail detail) {
		Relationship data = Relationship.builder().id(build.getDroplet().getId()).build();
		return DefaultCloudFoundryOperations.builder()
				.from(opsClient)
				.organization(detail.getOrganization())
				.space(detail.getSpace())
				.build()
					.getCloudFoundryClient()
					.applicationsV3()
						.setCurrentDroplet(SetApplicationCurrentDropletRequest.builder().applicationId(detail.getAppId()).data(data).build());
	}

	private Mono<Void> restartApp(AppDetail detail) {
		return DefaultCloudFoundryOperations.builder()
				.from(opsClient)
				.organization(detail.getOrganization())
				.space(detail.getSpace())
				.build()
					.applications()
						.restart(RestartApplicationRequest.builder().name(detail.getAppName()).build());
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

	private static Predicate<GetBuildResponse> isStagingComplete() {
        return response -> response.getState().equals(BuildState.STAGED) || response.getState().equals(BuildState.FAILED);
	}

	private static Predicate<GetBuildResponse> isStaged() {
        return response -> response.getState().equals(BuildState.STAGED);
    }

	@Builder
	@Getter
	static class BuildpackLifecycleData implements LifecycleData {

		private List<String> buildpacks;
		private String stack;
	}
}
