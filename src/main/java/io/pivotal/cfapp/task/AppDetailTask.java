package io.pivotal.cfapp.task;

import static org.cloudfoundry.util.tuple.TupleUtils.function;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.client.v2.applications.ApplicationStatisticsRequest;
import org.cloudfoundry.client.v2.applications.ApplicationStatisticsResponse;
import org.cloudfoundry.client.v2.applications.InstanceStatistics;
import org.cloudfoundry.client.v2.applications.Statistics;
import org.cloudfoundry.client.v2.applications.SummaryApplicationRequest;
import org.cloudfoundry.client.v2.applications.SummaryApplicationResponse;
import org.cloudfoundry.client.v2.applications.Usage;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationEvent;
import org.cloudfoundry.operations.applications.ApplicationSummary;
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
import io.pivotal.cfapp.service.StacksCache;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
@Component
public class AppDetailTask implements ApplicationListener<SpacesRetrievedEvent> {

    private PasSettings settings;
    private DefaultCloudFoundryOperations opsClient;
    private AppDetailService service;
    private BuildpacksCache buildpacksCache;
    private StacksCache stacksCache;
    private ApplicationEventPublisher publisher;

    @Autowired
    public AppDetailTask(
            PasSettings settings,
    		DefaultCloudFoundryOperations opsClient,
            AppDetailService service,
            BuildpacksCache buildpacksCache,
            StacksCache stacksCache,
    		ApplicationEventPublisher publisher) {
        this.settings = settings;
        this.opsClient = opsClient;
        this.service = service;
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
        service
            .deleteAll()
            .thenMany(Flux.fromIterable(spaces))
            .flatMap(space -> buildClient(space))
            .flatMap(client -> getApplicationSummary(client))
            .flatMap(tuple -> getApplicationDetail(tuple))
            .flatMap(service::save)
            .thenMany(service.findAll())
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

	private Mono<DefaultCloudFoundryOperations> buildClient(Space target) {
        log.trace("Targeting org={} and space={}", target.getOrganization(), target.getSpace());
        return Mono
                .just(DefaultCloudFoundryOperations
                        .builder()
                        .from(opsClient)
                        .organization(target.getOrganization())
		                .space(target.getSpace())
		                .build());
    }

    protected Flux<Tuple2<DefaultCloudFoundryOperations, ApplicationSummary>> getApplicationSummary(DefaultCloudFoundryOperations opsClient) {
        return
            opsClient
                .applications()
                    .list()
                    .map(a -> Tuples.of(opsClient, a));
    }

    protected Mono<AppDetail> getApplicationDetail(
        Tuple2<DefaultCloudFoundryOperations, ApplicationSummary> tuple
    ) {
        DefaultCloudFoundryOperations opsClient = tuple.getT1();
        ApplicationSummary summary = tuple.getT2();
        log.trace("Fetching application details for id={}, name={}", summary.getId(), summary.getName());
        return
            Mono.zipDelayError(
                Mono.just(opsClient),
                Mono.just(summary),
                getSummaryApplicationResponse(opsClient, summary.getId()),
                settings.isApplicationStatisticsEnabled() && summary.getRequestedState().equalsIgnoreCase("started")
                    ? getApplicationStatistics(opsClient, summary.getId())
                    : Mono.just(ApplicationStatisticsResponse.builder().build()),
                settings.isApplicationEventsEnabled()
                    ? getLastAppEvent(getAppEvents(opsClient, summary.getName()))
                    : Mono.just(Event.builder().build())
            )
            .onErrorResume(ex -> {
                log.warn(
                    String.format("Could not obtain application details for organization=%s, space=%s, and applicationName=%s",
                        opsClient.getOrganization(), opsClient.getSpace(), summary.getName()), ex);
                return
                    Mono.zip(
                        Mono.just(opsClient),
                        Mono.just(summary),
                        getSummaryApplicationResponse(opsClient, summary.getId()),
                        Mono.just(ApplicationStatisticsResponse.builder().build()),
                        Mono.just(Event.builder().build()));
            })
            .map(function(this::toAppDetail));
    }

    protected Mono<SummaryApplicationResponse> getSummaryApplicationResponse(DefaultCloudFoundryOperations opsClient, String applicationId) {
        return opsClient
                .getCloudFoundryClient()
                .applicationsV2()
                    .summary(SummaryApplicationRequest.builder().applicationId(applicationId).build());
    }

    protected Mono<ApplicationStatisticsResponse> getApplicationStatistics(DefaultCloudFoundryOperations opsClient, String applicationId) {
        return opsClient
                .getCloudFoundryClient()
                .applicationsV2()
                    .statistics(ApplicationStatisticsRequest.builder().applicationId(applicationId).build());
    }

    protected Flux<ApplicationEvent> getAppEvents(DefaultCloudFoundryOperations opsClient, String applicationName) {
        return opsClient
                .applications()
                    .getEvents(GetApplicationEventsRequest.builder().name(applicationName).maxNumberOfEvents(5).build());
    }

    protected Mono<Event> getLastAppEvent(Flux<ApplicationEvent> events) {
        return events
                .next()
                .flatMap(
                    e -> Mono.just(
                        Event.builder().type(e.getEvent()).actor(e.getActor())
                            .time(nullSafeLocalDateTime(e.getTime()))
                            .build()));
    }

    private AppDetail toAppDetail(
        DefaultCloudFoundryOperations opsClient,
        ApplicationSummary summary, SummaryApplicationResponse detail,
        ApplicationStatisticsResponse stats, Event event) {
        return AppDetail
                .builder()
                    .organization(opsClient.getOrganization())
                    .space(opsClient.getSpace())
                    .appId(summary.getId())
                    .appName(summary.getName())
                    .buildpack(detemineBuildpack(detail.getDetectedBuildpack(),detail.getBuildpack(), detail.getDockerImage()))
                    .buildpackVersion(nullSafeBuildpackVersion(detail.getDetectedBuildpackId()))
                    .image(detail.getDockerImage())
                    .stack(nullSafeStack(detail.getStackId()))
                    .runningInstances(nullSafeInteger(detail.getRunningInstances()))
                    .totalInstances(nullSafeInteger(detail.getInstances()))
                    .diskUsed(nullSafeDiskUsed(stats))
                    .memoryUsed(nullSafeMemoryUsed(stats))
                    .urls(summary.getUrls())
                    .lastEvent(event.getType())
                    .lastEventActor(event.getActor())
                    .lastEventTime(event.getTime())
                    .lastPushed(nullSafeLocalDateTime(detail.getPackageUpdatedAt()))
                    .requestedState(nullSafeString(summary.getRequestedState()).toLowerCase())
                .build();
    }

    private String detemineBuildpack(String detectedBuildpack, String plainOldBuildpack, String dockerImage) {
        String detected = settings.getBuildpack(detectedBuildpack, dockerImage);
        String plainOld = settings.getBuildpack(plainOldBuildpack, dockerImage);
        return detected != null && detected.equals("none") ? plainOld : detected;
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

    private static String nullSafeString(String value) {
        return value == null ? "": value;
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

    private String nullSafeBuildpackVersion(String buildpackId) {
        Buildpack buildpack = buildpacksCache.getBuildpackById(buildpackId);
        if (buildpack != null) {
            return buildpack.getVersion();
        }
        return "";
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
