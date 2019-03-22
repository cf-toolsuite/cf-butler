package io.pivotal.cfapp.task;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.GetApplicationEventsRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppEvent;
import io.pivotal.cfapp.domain.AppRequest;
import io.pivotal.cfapp.domain.Buildpack;
import io.pivotal.cfapp.domain.AppDetail.AppDetailBuilder;
import io.pivotal.cfapp.service.AppDetailService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

@Component
public class AppDetailTask implements ApplicationRunner {

    private DefaultCloudFoundryOperations opsClient;
    private AppDetailService service;
    private ApplicationEventPublisher publisher;

    @Autowired
    public AppDetailTask(
    		DefaultCloudFoundryOperations opsClient,
    		AppDetailService service,
    		ApplicationEventPublisher publisher) {
        this.opsClient = opsClient;
        this.service = service;
        this.publisher = publisher;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        collect();
    }

    public void collect() {
    	Hooks.onOperatorDebug();
        service
            .deleteAll()
            .thenMany(getOrganizations())
            .flatMap(spaceRequest -> getSpaces(spaceRequest))
            .flatMap(appSummaryRequest -> getApplicationSummary(appSummaryRequest))
            .flatMap(appDetailRequest -> getApplicationDetail(appDetailRequest))
            .flatMap(withLastEventRequest -> enrichWithAppEvent(withLastEventRequest))
            .flatMap(service::save)
            .collectList()
            .subscribe(r ->
                publisher.publishEvent(
                    new AppDetailRetrievedEvent(this)
                        .detail(r)
                )
            );
    }

    @Scheduled(cron = "${cron.collection}")
    protected void runTask() {
        collect();
    }

    protected Flux<AppRequest> getOrganizations() {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .build()
                .organizations()
                    .list()
                    .map(os -> AppRequest.builder().organization(os.getName()).build());
    }

    protected Flux<AppRequest> getSpaces(AppRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .build()
                .spaces()
                    .list()
                    .map(ss -> AppRequest.from(request).space(ss.getName()).build());
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

    // Added onErrorResume as per https://stackoverflow.com/questions/48243630/is-there-a-way-in-reactor-to-ignore-error-signals
    // to address org.cloudfoundry.client.v2.ClientV2Exception: CF-NoAppDetectedError(170003): An app was not successfully detected by any available buildpack
    // which results in some undesirable but tolerable data loss
    protected Mono<AppDetail> getApplicationDetail(AppRequest request) {
         return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .space(request.getSpace())
            .build()
                .applications()
                    .get(GetApplicationRequest.builder().name(request.getAppName()).build())
                    .onErrorResume(e -> Mono.empty())
                    .map(a -> fromApplicationDetail(a, request));
    }

    private AppDetail fromApplicationDetail(ApplicationDetail a, AppRequest request) {
        AppDetailBuilder builder = AppDetail.builder();
        builder
            .organization(request.getOrganization())
            .space(request.getSpace())
            .appId(request.getId())
            .appName(request.getAppName());
        String buildpack = Buildpack.is(a.getBuildpack(), request.getImage());
        if (buildpack != null)  {
            builder.buildpack(buildpack);
        }
        return builder
            .image(request.getImage())
            .stack(a.getStack())
            .runningInstances(a.getRunningInstances())
            .totalInstances(a.getInstances())
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
}
