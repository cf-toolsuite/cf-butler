package io.pivotal.cfapp.task;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.client.v2.ClientV2Exception;
import org.cloudfoundry.client.v2.applications.ApplicationStatisticsRequest;
import org.cloudfoundry.client.v2.applications.ApplicationStatisticsResponse;
import org.cloudfoundry.client.v2.applications.InstanceStatistics;
import org.cloudfoundry.client.v2.applications.Statistics;
import org.cloudfoundry.client.v2.applications.SummaryApplicationRequest;
import org.cloudfoundry.client.v2.applications.Usage;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.GetApplicationEventsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.Buildpack;
import io.pivotal.cfapp.domain.Event;
import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.domain.Stack;
import io.pivotal.cfapp.event.AppDetailRetrievedEvent;
import io.pivotal.cfapp.event.SpacesRetrievedEvent;
import io.pivotal.cfapp.service.AppDetailService;
import io.pivotal.cfapp.service.BuildpacksCache;
import io.pivotal.cfapp.service.EventsService;
import io.pivotal.cfapp.service.StacksCache;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AppDetailTask implements ApplicationListener<SpacesRetrievedEvent> {

    private final PasSettings settings;
    private final DefaultCloudFoundryOperations opsClient;
    private final AppDetailService appDetailsService;
    private final EventsService eventsService;
    private final BuildpacksCache buildpacksCache;
    private final StacksCache stacksCache;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public AppDetailTask(
            PasSettings settings,
    		DefaultCloudFoundryOperations opsClient,
            AppDetailService appDetailsService,
            EventsService eventsService,
            BuildpacksCache buildpacksCache,
            StacksCache stacksCache,
    		ApplicationEventPublisher publisher) {
        this.settings = settings;
        this.opsClient = opsClient;
        this.appDetailsService = appDetailsService;
        this.eventsService = eventsService;
        this.buildpacksCache = buildpacksCache;
        this.stacksCache = stacksCache;
        this.publisher = publisher;
    }

    @Override
    public void onApplicationEvent(SpacesRetrievedEvent event) {
        collect(List.copyOf(event.getSpaces()));
    }

    public void collect(List<Space> spaces) {
        log.info("AppDetailTask started");
        appDetailsService
            .deleteAll()
            .thenMany(Flux.fromIterable(spaces))
            .concatMap(space -> listApplications(space))
            .flatMap(fragment -> getSummaryInfo(fragment))
            .flatMap(fragment -> getStatistics(fragment))
            .flatMap(fragment -> getLastEvent(fragment))
            .flatMap(appDetailsService::save)
            .thenMany(appDetailsService.findAll())
                .collectList()
                .subscribe(
                    result -> {
                        publisher.publishEvent(new AppDetailRetrievedEvent(this).detail(result));
                        log.info("AppDetailTask completed");
                    },
                    error -> {
                        log.error("AppDetailTask terminated with error", error);
                    }
                );
    }

	private DefaultCloudFoundryOperations buildClient(Space target) {
        return DefaultCloudFoundryOperations
                .builder()
                .from(opsClient)
                .organization(target.getOrganizationName())
                .space(target.getSpaceName())
                .build();
    }

    private static Space buildSpace(String organization, String space) {
        return Space
                .builder()
                    .organizationName(organization)
                    .spaceName(space)
                .build();
    }

    protected Flux<AppDetail> listApplications(Space target) {
        return
            buildClient(target)
                .applications()
                    .list()
                    .flatMap(as ->
                        Mono
                            .just(AppDetail
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
                            )
                    );
    }

    protected Mono<AppDetail> getSummaryInfo(AppDetail fragment) {
        log.trace("Fetching application summary for org={}, space={}, id={}, name={}", fragment.getOrganization(), fragment.getSpace(), fragment.getAppId(), fragment.getAppName());
        return opsClient
                .getCloudFoundryClient()
                    .applicationsV2()
                        .summary(SummaryApplicationRequest.builder().applicationId(fragment.getAppId()).build())
                        .flatMap(sar ->
                            Mono
                                .just(AppDetail
                                        .from(fragment)
                                            .buildpack(getBuildpack(sar.getDetectedBuildpackId()))
                                            .buildpackVersion(getBuildpackVersion(sar.getDetectedBuildpackId()))
                                            .image(sar.getDockerImage())
                                            .stack(nullSafeStack(sar.getStackId()))
                                            .lastPushed(nullSafeLocalDateTime(sar.getPackageUpdatedAt()))
                                        .build()
                                )
                        );
    }

    protected Mono<AppDetail> getStatistics(AppDetail fragment) {
        log.trace("Fetching application statistics for org={}, space={}, id={}, name={}", fragment.getOrganization(), fragment.getSpace(), fragment.getAppId(), fragment.getAppName());
        return buildClient(buildSpace(fragment.getOrganization(), fragment.getSpace()))
                .getCloudFoundryClient()
                    .applicationsV2()
                        .statistics(ApplicationStatisticsRequest.builder().applicationId(fragment.getAppId()).build())
                        .flatMap(stats ->
                            Mono
                                .just(AppDetail
                                        .from(fragment)
                                            .diskUsed(nullSafeDiskUsed(stats))
                                            .memoryUsed(nullSafeMemoryUsed(stats))
                                        .build()
                                )
                        )
                        .onErrorResume(ClientV2Exception.class, e -> Mono.just(fragment));

    }

    protected Mono<AppDetail> getLastEvent(AppDetail fragment) {
        log.trace("Fetching last event for application id={}, name={} in org={}, space={}", fragment.getAppId(), fragment.getAppName(), fragment.getOrganization(), fragment.getSpace());
        return eventsService.getEvents(fragment.getAppId(), 1)
                    .flatMapMany(envelope -> eventsService.toFlux(envelope))
                    .next()
                    .flatMap(
                        e -> Mono.just(
                            AppDetail
                                .from(fragment)
                                    .lastEvent(e.getType())
                                    .lastEventActor(e.getActor())
                                    .lastEventTime(e.getTime())
                                .build()))
                    .defaultIfEmpty(fragment);
    }

    private String getBuildpack(String buildpackId) {
        Buildpack buildpack = buildpacksCache.getBuildpackById(buildpackId);
        if (buildpack != null) {
            return settings.getBuildpack(buildpack.getName());
        }
        return null;
    }

    private String getBuildpackVersion(String buildpackId) {
        Buildpack buildpack = buildpacksCache.getBuildpackById(buildpackId);
        if (buildpack != null) {
            return buildpack.getVersion();
        }
        return null;
    }

	private static LocalDateTime nullSafeLocalDateTime(String value) {
        return StringUtils.isNotBlank(value)
            ? Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDateTime()
            : null;
    }

    private static LocalDateTime nullSafeLocalDateTime(Date value) {
        return value != null
            ? value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            : null;
    }

    private static Integer nullSafeInteger(Integer value) {
        return value != null ? value: 0;
    }

    private String nullSafeStack(String stackId) {
        Stack stack = stacksCache.getStackById(stackId);
        if (stack != null) {
            return stack.getName();
        }
        return "unknown";
    }

    private static Long nullSafeMemoryUsed(ApplicationStatisticsResponse stats) {
        Long result = 0L;
        Map<String, InstanceStatistics> instances = stats.getInstances();
        if (instances != null) {
            Statistics innerStats;
            for (InstanceStatistics is: instances.values()) {
                innerStats = is.getStatistics();
                Usage usage;
                if (innerStats != null) {
                    usage = innerStats.getUsage();
                    if (usage != null && usage.getMemory() != null) {
                        result += usage.getMemory();
                    }
                }
            }
        }
        return result;
	}

	private static Long nullSafeDiskUsed(ApplicationStatisticsResponse stats) {
        Long result = 0L;
        Map<String, InstanceStatistics> instances = stats.getInstances();
        if (instances != null) {
            Statistics innerStats;
            for (InstanceStatistics is: instances.values()) {
                innerStats = is.getStatistics();
                Usage usage;
                if (innerStats != null) {
                    usage = innerStats.getUsage();
                    if (usage != null && usage.getDisk() != null) {
                        result += usage.getDisk();
                    }
                }
            }
        }
        return result;
	}

}
