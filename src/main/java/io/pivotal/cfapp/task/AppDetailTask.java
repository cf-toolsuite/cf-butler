package io.pivotal.cfapp.task;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.GetApplicationEventsRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppEvent;
import io.pivotal.cfapp.domain.AppRequest;
import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.service.AppDetailService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
        service
            .deleteAll()
            .thenMany(Flux.fromIterable(spaces))
            .map(s -> AppRequest.builder().organization(s.getOrganization()).space(s.getSpace()).build())
            .flatMap(appSummaryRequest -> getApplicationSummary(appSummaryRequest))
            .flatMap(appManifestRequest -> getDockerImage(appManifestRequest))
            .flatMap(appDetailRequest -> getApplicationDetail(appDetailRequest))
            .flatMap(withLastEventRequest -> enrichWithAppEvent(withLastEventRequest))
            .publishOn(Schedulers.parallel())
            .flatMap(service::save)
            .thenMany(service.findAll().subscribeOn(Schedulers.elastic()))
                .collectList()
                .subscribe(r ->
                    publisher.publishEvent(
                        new AppDetailRetrievedEvent(this)
                            .detail(r)
                ));
    }

    protected Flux<AppRequest> getApplicationSummary(AppRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .space(request.getSpace())
            .build()
                .applications()
                    .list()
                    .map(as -> AppRequest.from(request).id(as.getId()).appName(as.getName()).build());
    }

    protected Mono<AppRequest> getDockerImage(AppRequest request) {
        return opsClient
                .getCloudFoundryClient()
				.applicationsV2()
					.get(org.cloudfoundry.client.v2.applications.GetApplicationRequest.builder().applicationId(request.getId()).build())
	    			.onErrorResume(err -> Mono.empty())
	    			.map(gar -> AppRequest.from(request).image(gar.getEntity().getDockerImage()).build());
    }

    protected Mono<AppDetail> getApplicationDetail(AppRequest request) {
         return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .space(request.getSpace())
            .build()
                .applications()
                    .get(GetApplicationRequest.builder().name(request.getAppName()).build())
                    .onErrorResume(err -> Mono.empty())
                    .map(a -> fromApplicationDetail(a, request));
    }

    private AppDetail fromApplicationDetail(ApplicationDetail a, AppRequest request) {
        return AppDetail.builder()
            .organization(request.getOrganization())
            .space(request.getSpace())
            .appId(request.getId())
            .appName(request.getAppName())
            .buildpack(settings.getBuildpack(a.getBuildpack(), request.getImage()))
            .image(request.getImage())
            .stack(a.getStack())
            .runningInstances(nullSafeInteger(a.getRunningInstances()))
            .totalInstances(nullSafeInteger(a.getInstances()))
            .diskUsage(a.getInstanceDetails().stream().mapToLong(id -> nullSafeLong(id.getDiskUsage())).sum())
            .memoryUsage(a.getInstanceDetails().stream().mapToLong(id -> nullSafeLong(id.getMemoryUsage())).sum())
            .urls(a.getUrls())
            .lastPushed(a.getLastUploaded() != null ? a.getLastUploaded()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime(): LocalDateTime.MIN)
            .requestedState(a.getRequestedState().toLowerCase())
            .build();
    }

    protected Mono<AppDetail> enrichWithAppEvent(AppDetail detail) {
        return DefaultCloudFoundryOperations.builder()
           .from(opsClient)
           .organization(detail.getOrganization())
           .space(detail.getSpace())
           .build()
               .applications()
                   .getEvents(GetApplicationEventsRequest.builder().name(detail.getAppName()).build())
                       .map(e -> AppEvent
                                   .builder()
                                       .name(e.getEvent())
                                       .actor(e.getActor())
                                       .time(e.getTime())
                                       .build())
                       .next()
                       .map(e ->
                               AppDetail.from(detail)
                                           .lastEvent(e.getName())
                                           .lastEventActor(e.getActor())
                                           .lastEventTime(e.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                                           .build()
                           )
                       .switchIfEmpty(Mono.just(detail));
    }

    private Long nullSafeLong(Long input) {
        return input != null ? input: 0L;
    }

    private Integer nullSafeInteger(Integer input) {
        return input != null ? input: 0;
    }

}
