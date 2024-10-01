package org.cftoolsuite.cfapp.task;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.domain.Buildpack;
import org.cftoolsuite.cfapp.domain.Space;
import org.cftoolsuite.cfapp.domain.Stack;
import org.cftoolsuite.cfapp.domain.product.PivnetCache;
import org.cftoolsuite.cfapp.event.AppDetailReadyToBeRetrievedEvent;
import org.cftoolsuite.cfapp.event.AppDetailRetrievedEvent;
import org.cftoolsuite.cfapp.service.AppDetailService;
import org.cftoolsuite.cfapp.service.BuildpacksCache;
import org.cftoolsuite.cfapp.service.EventsService;
import org.cftoolsuite.cfapp.service.StacksCache;
import org.cloudfoundry.client.v2.ClientV2Exception;
import org.cloudfoundry.client.v2.applications.SummaryApplicationRequest;
import org.cloudfoundry.client.v3.ClientV3Exception;
import org.cloudfoundry.client.v3.applications.GetApplicationCurrentDropletRequest;
import org.cloudfoundry.client.v3.applications.GetApplicationCurrentDropletResponse;
import org.cloudfoundry.client.v3.applications.GetApplicationProcessStatisticsRequest;
import org.cloudfoundry.client.v3.processes.ProcessState;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AppDetailTask implements ApplicationListener<AppDetailReadyToBeRetrievedEvent> {

    private final PasSettings settings;
    private final DefaultCloudFoundryOperations opsClient;
    private final AppDetailService appDetailsService;
    private final EventsService eventsService;
    private final BuildpacksCache buildpacksCache;
    private final StacksCache stacksCache;
    private final ApplicationEventPublisher publisher;
    private final PivnetCache pivnetCache;
    private final AppDetailReadyToBeCollectedDecider appDetailReadyToBeCollectedDecider;

    @Autowired
    public AppDetailTask(
            PivnetCache pivnetCache,
            PasSettings settings,
            DefaultCloudFoundryOperations opsClient,
            AppDetailService appDetailsService,
            EventsService eventsService,
            BuildpacksCache buildpacksCache,
            StacksCache stacksCache,
            ApplicationEventPublisher publisher,
            AppDetailReadyToBeCollectedDecider appDetailReadyToBeCollectedDecider) {
        this.pivnetCache = pivnetCache;
        this.settings = settings;
        this.opsClient = opsClient;
        this.appDetailsService = appDetailsService;
        this.eventsService = eventsService;
        this.buildpacksCache = buildpacksCache;
        this.stacksCache = stacksCache;
        this.publisher = publisher;
        this.appDetailReadyToBeCollectedDecider = appDetailReadyToBeCollectedDecider;
    }

    private DefaultCloudFoundryOperations buildClient(Space target) {
        return DefaultCloudFoundryOperations
                .builder()
                .from(opsClient)
                .organization(target.getOrganizationName())
                .space(target.getSpaceName())
                .build();
    }

    public void collect(List<Space> spaces) {
        log.info("AppDetailTask started");
        appDetailsService
            .deleteAll()
            .thenMany(Flux.fromIterable(spaces))
            .concatMap(this::listApplications)
            .flatMap(this::getSummaryInfo)
            .flatMap(this::getBuildpackFromApplicationCurrentDroplet)
            .flatMap(this::getUsageAndQuotas)
            .flatMap(this::getLastEvent)
            .flatMap(appDetailsService::save)
            .thenMany(appDetailsService.findAll())
            .collectList()
            .subscribe(
                result -> {
                    publisher.publishEvent(new AppDetailRetrievedEvent(this).detail(result));
                    log.info("AppDetailTask completed. {} applications found.", result.size());
                },
                error -> {
                    log.error("AppDetailTask terminated with error", error);
                }
            );
    }

    private String getBuildpack(String buildpackId) {
        Buildpack buildpack = buildpacksCache.getBuildpackById(buildpackId);
        if (buildpack != null) {
            return settings.getBuildpack(buildpack.getName());
        }
        return null;
    }

    private String getBuildpackLatestVersion(String buildpackId) {
        return pivnetCache.findLatestProductReleaseBySlug(getBuildpack(buildpackId) + "-buildpack").getVersion();
    }

    private LocalDateTime getBuildpackReleaseDate(String buildpackId) {
        LocalDate releaseDate = pivnetCache.findLatestProductReleaseBySlug(getBuildpack(buildpackId) + "-buildpack").getReleaseDate();
        if (releaseDate != null){
            return releaseDate.atStartOfDay();
        }
        return null;
    }

    private String getBuildpackReleaseNotesUrl(String buildpackId) {
        return pivnetCache.findLatestProductReleaseBySlug(getBuildpack(buildpackId) + "-buildpack").getReleaseNotesUrl();
    }

    private String getBuildpackReleaseType(String buildpackId) {
        return pivnetCache.findLatestProductReleaseBySlug(getBuildpack(buildpackId) + "-buildpack").getReleaseType();
    }

    private String getBuildpackVersion(String buildpackId) {
        Buildpack buildpack = buildpacksCache.getBuildpackById(buildpackId);
        if (buildpack != null) {
            return buildpack.getVersion();
        }
        return null;
    }

    private String getCurrentDropletBuildpackVersion(String raw) {
        String version = raw;
        if (version != null) {
            if (version.contains("-")) {
                version = raw.substring(0, raw.indexOf("-"));
            }
        }
        return version;
    }

    protected Mono<AppDetail> getLastEvent(AppDetail fragment) {
        log.trace("Fetching last event for application id={}, name={} in org={}, space={}", fragment.getAppId(), fragment.getAppName(), fragment.getOrganization(), fragment.getSpace());
        return eventsService.getEvents(fragment.getAppId(), 1)
                .flatMapMany(envelope -> eventsService.toFlux(envelope))
                .next()
                .map(e ->
                    AppDetail
                        .from(fragment)
                        .lastEvent(e.getType())
                        .lastEventActor(e.getActor())
                        .lastEventTime(e.getTime())
                        .build()
                )
                .defaultIfEmpty(fragment);
    }


    protected Mono<AppDetail> getUsageAndQuotas(AppDetail fragment) {
        log.trace("Fetching application usage stats and quotas for org={}, space={}, id={}, name={}",
                fragment.getOrganization(), fragment.getSpace(), fragment.getAppId(), fragment.getAppName());

        return buildClient(buildSpace(fragment.getOrganization(), fragment.getSpace()))
                .getCloudFoundryClient()
                .applicationsV3()
                .getProcessStatistics(GetApplicationProcessStatisticsRequest.builder()
                        .applicationId(fragment.getAppId())
                        .type("web")
                        .build())
                .map(stats -> {
                    Long diskUsage = stats.getResources().stream()
                            .filter(r -> r.getState().equals(ProcessState.RUNNING))
                            .findFirst()
                            .map(r -> nullSafeLong(r.getUsage().getDisk()))
                            .orElse(0L);
                    Long memoryUsage = stats.getResources().stream()
                            .filter(r -> r.getState().equals(ProcessState.RUNNING))
                            .findFirst()
                            .map(r -> nullSafeLong(r.getUsage().getMemory()))
                            .orElse(0L);
                    Long diskQuota = stats.getResources().stream()
                            .filter(r -> r.getState().equals(ProcessState.RUNNING))
                            .findFirst()
                            .map(r -> nullSafeLong(r.getDiskQuota()))
                            .orElse(0L);
                    Long memoryQuota = stats.getResources().stream()
                            .filter(r -> r.getState().equals(ProcessState.RUNNING))
                            .findFirst()
                            .map(r -> nullSafeLong(r.getMemoryQuota()))
                            .orElse(0L);
                    return
                        AppDetail
                        .from(fragment)
                        .memoryUsed(memoryUsage)
                        .diskUsed(diskUsage)
                        .diskQuota(diskQuota)
                        .memoryQuota(memoryQuota)
                        .build();
                })
                .onErrorResume(ClientV3Exception.class, e -> Mono.just(fragment));
    }

    protected Mono<AppDetail> getSummaryInfo(AppDetail fragment) {
        log.trace("Fetching application summary for org={}, space={}, id={}, name={}", fragment.getOrganization(), fragment.getSpace(), fragment.getAppId(), fragment.getAppName());
        return opsClient
                .getCloudFoundryClient()
                .applicationsV2()
                .summary(SummaryApplicationRequest.builder().applicationId(fragment.getAppId()).build())
                .map(sar ->
                    AppDetail
                        .from(fragment)
                        .buildpack(getBuildpack(sar.getDetectedBuildpackId()))
                        .buildpackVersion(getBuildpackVersion(sar.getDetectedBuildpackId()))
                        .image(sar.getDockerImage())
                        .stack(nullSafeStack(sar.getStackId()))
                        .lastPushed(nullSafeLocalDateTime(sar.getPackageUpdatedAt()))
                        .buildpackReleaseType(getBuildpackReleaseType(sar.getDetectedBuildpackId()))
                        .buildpackReleaseDate(getBuildpackReleaseDate(sar.getDetectedBuildpackId()))
                        .buildpackLatestVersion(getBuildpackLatestVersion(sar.getDetectedBuildpackId()))
                        .buildpackLatestUrl(getBuildpackReleaseNotesUrl(sar.getDetectedBuildpackId()))
                        .build()
                )
                .onErrorResume(ClientV2Exception.class, e -> Mono.just(fragment));
    }

    protected Mono<AppDetail> getBuildpackFromApplicationCurrentDroplet(AppDetail fragment) {
        if (StringUtils.isNotBlank(fragment.getBuildpack())) {
            log.trace("Fetching application current droplet buildpack for org={}, space={}, id={}, name={}", fragment.getOrganization(), fragment.getSpace(), fragment.getAppId(), fragment.getAppName());
            return opsClient
                    .getCloudFoundryClient()
                    .applicationsV3()
                    .getCurrentDroplet(GetApplicationCurrentDropletRequest.builder().applicationId(fragment.getAppId()).build())
                    .map(acd -> refineBuildpackFromApplicationCurrentDroplet(fragment, acd))
                    .onErrorResume(ClientV3Exception.class, e -> Mono.just(fragment));
        }
        return Mono.just(fragment);
    }

    private AppDetail refineBuildpackFromApplicationCurrentDroplet(AppDetail fragment, GetApplicationCurrentDropletResponse response) {
        if (fragment.getBuildpack().equals("meta")) {
            List<String> buildpackNameFragments = Arrays.asList(response.getBuildpacks().get(0).getBuildpackName().split(" "));
            if (!CollectionUtils.isEmpty(buildpackNameFragments)) {
                String buildpack = buildpackNameFragments.stream().filter(bnf -> bnf.contains("buildpack")).collect(Collectors.toList()).get(0);
                String[] parts = buildpack.split("=");
                if (parts.length == 2) {
                    return
                        AppDetail
                            .from(fragment)
                            .buildpack(settings.getBuildpack(parts[0]))
                            .buildpackVersion(getCurrentDropletBuildpackVersion(parts[1]))
                            .build();
                }
            }
        }
        return
            AppDetail
                .from(fragment)
                .buildpack(settings.getBuildpack(response.getBuildpacks().get(0).getBuildpackName()))
                .buildpackVersion(getCurrentDropletBuildpackVersion(response.getBuildpacks().get(0).getVersion()))
                .build();
    }

    @Override
    public void onApplicationEvent(AppDetailReadyToBeRetrievedEvent event) {
        if (appDetailReadyToBeCollectedDecider.isDecided()) {
            collect(List.copyOf(appDetailReadyToBeCollectedDecider.getSpaces()));
            appDetailReadyToBeCollectedDecider.reset();
        }
    }

    protected Flux<AppDetail> listApplications(Space target) {
        return
            buildClient(target)
                .applications()
                .list()
                .map(as ->
                    AppDetail
                        .builder()
                        .appId(as.getId())
                        .appName(as.getName())
                        .organization(target.getOrganizationName())
                        .space(target.getSpaceName())
                        .runningInstances(nullSafeInteger(as.getRunningInstances()))
                        .totalInstances(nullSafeInteger(as.getInstances()))
                        .requestedState(as.getRequestedState().toLowerCase())
                        .urls(as.getUrls())
                        .build()
                );
    }

    private String nullSafeStack(String stackId) {
        Stack stack = stacksCache.getStackById(stackId);
        if (stack != null) {
            return stack.getName();
        }
        return "unknown";
    }

    private static Space buildSpace(String organization, String space) {
        return Space
                .builder()
                .organizationName(organization)
                .spaceName(space)
                .build();
    }

    private static Integer nullSafeInteger(Integer value) {
        return value != null ? value: 0;
    }

    private static Long nullSafeLong(Long value) {
        return value != null ? value: 0L;
    }

    private static LocalDateTime nullSafeLocalDateTime(String value) {
        return StringUtils.isNotBlank(value)
                ? Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : null;
    }

}
