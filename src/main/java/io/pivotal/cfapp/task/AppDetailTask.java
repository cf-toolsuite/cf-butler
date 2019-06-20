package io.pivotal.cfapp.task;

import java.time.ZoneId;
import java.util.List;

import org.cloudfoundry.client.v2.applications.SummaryApplicationRequest;
import org.cloudfoundry.client.v2.applications.SummaryApplicationResponse;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.GetApplicationEventsRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppEvent;
import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.service.AppDetailService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple5;

import static org.cloudfoundry.util.tuple.TupleUtils.function;

@Slf4j
@Component
public class AppDetailTask implements ApplicationListener<SpacesRetrievedEvent> {

    private ButlerSettings settings;
    private DefaultCloudFoundryOperations opsClient;
    private AppDetailService service;
    private ApplicationEventPublisher publisher;

    @Autowired
    public AppDetailTask(
            ButlerSettings settings,
    		DefaultCloudFoundryOperations opsClient,
    		AppDetailService service,
    		ApplicationEventPublisher publisher) {
        this.settings = settings;
        this.opsClient = opsClient;
        this.service = service;
        this.publisher = publisher;
    }

    @Override
    public void onApplicationEvent(SpacesRetrievedEvent event) {
        collect(List.copyOf(event.getSpaces()));
    }

    public void collect(List<Space> spaces) {
        Hooks.onOperatorDebug();
        log.info("AppDetailTask started.");
        service
            .deleteAll()
            .thenMany(Flux.fromIterable(spaces))
            .map(s -> DefaultCloudFoundryOperations.builder()
                        .from(opsClient)
                        .organization(s.getOrganization())
                        .space(s.getSpace())
                        .build())
            .flatMap(opsClient -> getApplicationSummary(opsClient))
            .flatMap(tuple -> getAuxiliaryContent(tuple))
            .map(function(this::toAppDetail))
            .flatMap(service::save)
            .thenMany(service.findAll())
                .collectList()
                .subscribe(
                    result -> {
                        publisher.publishEvent(new AppDetailRetrievedEvent(this).detail(result));
                        log.info("AppDetailTask completed.");
                    },
                    error -> {
                        log.error("AppDetailTask terminated with error", error);
                    }
                );
    }

    protected Mono<Tuple5<DefaultCloudFoundryOperations, ApplicationSummary, ApplicationDetail, AppEvent, SummaryApplicationResponse>> getAuxiliaryContent(
        Tuple2<DefaultCloudFoundryOperations, ApplicationSummary> tuple
    ) {
        DefaultCloudFoundryOperations opsClient = tuple.getT1();
        ApplicationSummary summary = tuple.getT2();
        return Mono.zip(
            Mono.just(opsClient),
            Mono.just(summary),
            getApplicationDetail(opsClient, summary)
                .onErrorResume(ex -> {
                    log.warn(String.format("Trouble fetching application detail with org=%s, space=%s, summary=%s", opsClient.getOrganization(), opsClient.getSpace(), summary.toString()), ex);
                    return Mono.empty();
                }),
            getAppEvent(opsClient, summary),
            getSummaryApplicationResponse(opsClient, summary))
    }

    protected Flux<Tuple2<DefaultCloudFoundryOperations, ApplicationSummary>> getApplicationSummary(DefaultCloudFoundryOperations opsClient) {
        return
            Flux.zip(
                Mono.just(opsClient),
                opsClient
                    .applications()
                        .list());
    }

    protected Mono<ApplicationDetail> getApplicationDetail(DefaultCloudFoundryOperations opsClient, ApplicationSummary summary) {
        return opsClient
                .applications()
                    .get(GetApplicationRequest.builder().name(summary.getName()).build());
    }

    protected Mono<SummaryApplicationResponse> getSummaryApplicationResponse(DefaultCloudFoundryOperations opsClient, ApplicationSummary summary) {
        return opsClient
                .getCloudFoundryClient()
                .applicationsV2()
                    .summary(SummaryApplicationRequest.builder().applicationId(summary.getId()).build());
    }

    protected Mono<AppEvent> getAppEvent(DefaultCloudFoundryOperations opsClient, ApplicationSummary summary) {
        return opsClient
                    .applications()
                        .getEvents(GetApplicationEventsRequest.builder().name(summary.getName()).build())
                        .map(e -> AppEvent
                                    .builder()
                                        .name(e.getEvent())
                                        .actor(e.getActor())
                                        .time(
                                            e.getTime() != null ?
                                                e.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() :
                                                null)
                                        .build())
                        .next();
    }

    protected AppDetail toAppDetail(
        DefaultCloudFoundryOperations opsClient,
        ApplicationSummary summary, ApplicationDetail detail,
        AppEvent event, SummaryApplicationResponse response) {
        return AppDetail
                .builder()
                    .organization(opsClient.getOrganization())
                    .space(opsClient.getSpace())
                    .appId(summary.getId())
                    .appName(summary.getName())
                    .buildpack(settings.getBuildpack(detail.getBuildpack(), response.getDockerImage()))
                    .image(response.getDockerImage())
                    .stack(detail.getStack())
                    .runningInstances(nullSafeInteger(detail.getRunningInstances()))
                    .totalInstances(nullSafeInteger(detail.getInstances()))
                    .diskUsage(detail.getInstanceDetails() == null ? 0L : detail.getInstanceDetails().stream().mapToLong(id -> nullSafeLong(id.getDiskUsage())).sum())
                    .memoryUsage(detail.getInstanceDetails() == null ? 0L : detail.getInstanceDetails().stream().mapToLong(id -> nullSafeLong(id.getMemoryUsage())).sum())
                    .urls(detail.getUrls())
                    .lastPushed(detail.getLastUploaded() != null ? detail.getLastUploaded()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime(): null)
                    .requestedState(detail.getRequestedState() == null ? "": detail.getRequestedState().toLowerCase())
                .build();
    }

    private Long nullSafeLong(Long input) {
        return input != null ? input: 0L;
    }

    private Integer nullSafeInteger(Integer input) {
        return input != null ? input: 0;
    }

}
