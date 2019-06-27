package io.pivotal.cfapp.task;

import static org.cloudfoundry.util.tuple.TupleUtils.function;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppEvent;
import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.domain.Stack;
import io.pivotal.cfapp.repository.StacksCache;
import io.pivotal.cfapp.service.AppDetailService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
@Component
public class AppDetailTask implements ApplicationListener<SpacesRetrievedEvent> {

    private ButlerSettings settings;
    private DefaultCloudFoundryOperations opsClient;
    private AppDetailService service;
    private StacksCache stacksCache;
    private ApplicationEventPublisher publisher;

    @Autowired
    public AppDetailTask(
            ButlerSettings settings,
    		DefaultCloudFoundryOperations opsClient,
            AppDetailService service,
            StacksCache stacksCache,
    		ApplicationEventPublisher publisher) {
        this.settings = settings;
        this.opsClient = opsClient;
        this.service = service;
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

        Mono<DefaultCloudFoundryOperations> client = Mono.just(opsClient);
        Mono<ApplicationSummary> appSummary = Mono.just(summary);
        Mono<SummaryApplicationResponse> appDetails = getSummaryApplicationResponse(opsClient, summary.getId());
        Mono<ApplicationStatisticsResponse> appStatistics =
            summary.getRequestedState().equalsIgnoreCase("running")
                ? getApplicationStatistics(opsClient, summary.getId())
                : Mono.just(ApplicationStatisticsResponse.builder().build());
        Mono<AppEvent> appEvent = getLastAppEvent(getAppEvents(opsClient, summary.getName()));

        return
            Mono.zipDelayError(
                client,
                appSummary,
                appDetails,
                appStatistics,
                appEvent
            )
            .onErrorResume(ex -> {
                log.warn(
                    String.format("Could not obtain application details for organization=%s, space=%s, and applicationName=%s",
                        opsClient.getOrganization(), opsClient.getSpace(), summary.getName()), ex);
                return Mono.zip(client, appSummary, appDetails, Mono.just(ApplicationStatisticsResponse.builder().build()), Mono.just(AppEvent.builder().build()));
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

    protected Mono<AppEvent> getLastAppEvent(Flux<ApplicationEvent> events) {
        return events
                .next()
                .flatMap(
                    e -> Mono.just(
                        AppEvent.builder().name(e.getEvent()).actor(e.getActor())
                            .time(e.getTime() != null ? e.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                            .build()));
    }

    private AppDetail toAppDetail(
        DefaultCloudFoundryOperations opsClient,
        ApplicationSummary summary, SummaryApplicationResponse detail,
        ApplicationStatisticsResponse stats, AppEvent event) {
        return AppDetail
                .builder()
                    .organization(opsClient.getOrganization())
                    .space(opsClient.getSpace())
                    .appId(summary.getId())
                    .appName(summary.getName())
                    .buildpack(settings.getBuildpack(detail.getBuildpack(), detail.getDockerImage()))
                    .image(detail.getDockerImage())
                    .stack(nullSafeStack(detail.getStackId()))
                    .runningInstances(nullSafeInteger(detail.getRunningInstances()))
                    .totalInstances(nullSafeInteger(detail.getInstances()))
                    .diskUsage(nullSafeDiskUsage(stats))
                    .memoryUsage(nullSafeMemoryUsage(stats))
                    .urls(summary.getUrls())
                    .lastPushed(nullSafeLocalDateTime(detail.getPackageUpdatedAt()))
                    .requestedState(nullSafeString(summary.getRequestedState()).toLowerCase())
                .build();
    }

	private LocalDateTime nullSafeLocalDateTime(String value) {
        return StringUtils.isNotBlank(value) ? Instant.parse(value)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime() : null;
    }

    private String nullSafeString(String value) {
        return value == null ? "": value;
    }

    private Integer nullSafeInteger(Integer value) {
        return value != null ? value: 0;
    }

    private String nullSafeStack(String stackId) {
        Stack stack = stacksCache.getStack(stackId);
        if (stack != null) {
            return stack.getName();
        }
        return "unknown";
    }

    private Long nullSafeMemoryUsage(ApplicationStatisticsResponse stats) {
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

	private Long nullSafeDiskUsage(ApplicationStatisticsResponse stats) {
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
