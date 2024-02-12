package io.pivotal.cfapp.task;

import java.util.List;

import org.cloudfoundry.client.v3.droplets.DropletState;
import org.cloudfoundry.client.v3.droplets.ListDropletsRequest;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.JavaAppDetail;
import io.pivotal.cfapp.event.AppDetailRetrievedEvent;
import io.pivotal.cfapp.service.DropletsService;
import io.pivotal.cfapp.service.JavaAppDetailService;
import io.pivotal.cfapp.util.TgzUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PomFileExtractorTask implements ApplicationListener<AppDetailRetrievedEvent> {

    private DefaultCloudFoundryOperations opsClient;
    private DropletsService dropletsService;
    private JavaAppDetailService jadService;

    @Autowired
    public PomFileExtractorTask(
            DefaultCloudFoundryOperations opsClient,
            DropletsService dropletsService,
            JavaAppDetailService jadService
            ) {
        this.opsClient = opsClient;
        this.dropletsService = dropletsService;
        this.jadService = jadService;
    }

    public void collect(List<AppDetail> detail) {
        log.info("PomFileExtractorTask started");
        jadService
            .deleteAll()
            .thenMany(Flux.fromIterable(detail))
            .filter(ad -> ad.getBuildpack().contains("java"))
            .flatMap(ad -> seedJavaAppDetail(ad))
            .flatMap(jadService::save)
            .thenMany(jadService.findAll())
            .collectList()
            .subscribe(
                result -> {
                    log.info("PomFileExtractorTask completed");
                },
                error -> {
                    log.error("PomFileExtractorTask terminated with error", error);
                }
            );
    }

    private Mono<JavaAppDetail> seedJavaAppDetail(AppDetail detail) {
        log.info("Attempting to fetch droplet id for {}", detail.getAppName());
        return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                    .organization(detail.getOrganization())
                    .space(detail.getSpace())
                    .build()
                    .getCloudFoundryClient()
                    .droplets()
                    .list(ListDropletsRequest.builder().applicationId(detail.getAppId()).build())
                    .flatMapMany(response -> Flux.fromIterable(response.getResources()))
                    .filter(resource -> resource.getState().equals(DropletState.STAGED))
                    .next()
                    .map(dr -> JavaAppDetail.from(detail).dropletId(dr.getId()).build());
                    //.flatMap(jad -> getPomXmlContents(jad));
    }

    private Mono<JavaAppDetail> getPomXmlContents(JavaAppDetail detail) {
        return TgzUtil
                .extractFileContent(dropletsService.downloadDroplet(detail.getDropletId()), "pom.xml")
                .map(contents -> JavaAppDetail.from(detail).pomContents(contents).build());
    }

    @Override
    public void onApplicationEvent(AppDetailRetrievedEvent event) {
        collect(List.copyOf(event.getDetail()));
    }

}
