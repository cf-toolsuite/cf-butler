package io.pivotal.cfapp.task;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.client.v3.applications.GetApplicationCurrentDropletRequest;
import org.cloudfoundry.client.v3.applications.GetApplicationCurrentDropletResponse;
import org.cloudfoundry.client.v3.droplets.DropletResource;
import org.cloudfoundry.client.v3.droplets.DropletState;
import org.cloudfoundry.client.v3.droplets.ListDropletsRequest;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.JavaAppDetail;
import io.pivotal.cfapp.event.AppDetailRetrievedEvent;
import io.pivotal.cfapp.service.DropletsService;
import io.pivotal.cfapp.service.JavaAppDetailService;
import io.pivotal.cfapp.util.JavaArtifactReader;
import io.pivotal.cfapp.util.TgzUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ExtractJavaArtifactsFromDropletTask implements ApplicationListener<AppDetailRetrievedEvent> {

    private DefaultCloudFoundryOperations opsClient;
    private DropletsService dropletsService;
    private JavaAppDetailService jadService;
    private JavaArtifactReader javaArtifactReader;

    @Autowired
    public ExtractJavaArtifactsFromDropletTask(
            DefaultCloudFoundryOperations opsClient,
            DropletsService dropletsService,
            JavaAppDetailService jadService,
            JavaArtifactReader javaArtifactReader,
            Environment env
            ) {
        this.opsClient = opsClient;
        this.dropletsService = dropletsService;
        this.jadService = jadService;
        this.javaArtifactReader = javaArtifactReader;
    }

    public void collect(List<AppDetail> detail) {
        log.info("ExtractJavaArtifactsFromDropletTask started");
        jadService
            .deleteAll()
            .thenMany(Flux.fromIterable(detail))
            .filter(ad -> StringUtils.isNotBlank(ad.getBuildpack()) && ad.getBuildpack().contains("java"))
            .flatMap(ad -> seedJavaAppDetail(ad))
            .flatMap(jadService::save)
            .thenMany(jadService.findAll())
            .collectList()
            .subscribe(
                result -> {
                    log.info("ExtractJavaArtifactsFromDropletTask completed");
                },
                error -> {
                    log.error("ExtractJavaArtifactsFromDropletTask terminated with error", error);
                }
            );
    }

    private Mono<JavaAppDetail> seedJavaAppDetail(AppDetail detail) {
        log.info("Attempting to fetch droplet id for {}/{}/{} whose state is {}", detail.getOrganization(), detail.getSpace(), detail.getAppName(), detail.getRequestedState());
        if (detail.getRequestedState().equalsIgnoreCase("started")) {
            Mono<GetApplicationCurrentDropletResponse> currentResponse =
                DefaultCloudFoundryOperations.builder()
                    .from(opsClient)
                        .organization(detail.getOrganization())
                        .space(detail.getSpace())
                        .build()
                        .getCloudFoundryClient()
                        .applicationsV3()
                        .getCurrentDroplet(GetApplicationCurrentDropletRequest.builder().applicationId(detail.getAppId()).build());
            return currentResponse
                    .map(dr -> JavaAppDetail.from(detail).dropletId(dr.getId()).build())
                    .flatMap(jad -> ascertainSpringDependencies(jad));
        } else if (detail.getRequestedState().equalsIgnoreCase("stopped")) {
            Mono<DropletResource> stagedResponse =
                DefaultCloudFoundryOperations.builder()
                    .from(opsClient)
                        .organization(detail.getOrganization())
                        .space(detail.getSpace())
                        .build()
                        .getCloudFoundryClient()
                        .droplets()
                        .list(ListDropletsRequest.builder().applicationId(detail.getAppId()).build())
                        .flatMapMany(response -> Flux.fromIterable(response.getResources()))
                        .filter(resource -> resource.getState().equals(DropletState.STAGED))
                        .next();
                return stagedResponse
                        .map(dr -> JavaAppDetail.from(detail).dropletId(dr.getId()).build())
                        .flatMap(jad -> ascertainSpringDependencies(jad));
        } else {
            log.info("No droplet found for {}/{}/{}", detail.getOrganization(), detail.getSpace(), detail.getAppName());
            return Mono.just(JavaAppDetail.from(detail).build());
        }
    }

    private Mono<JavaAppDetail> ascertainSpringDependencies(JavaAppDetail detail) {
        Flux<DataBuffer> fdb = dropletsService.downloadDroplet(detail.getDropletId());
        if (javaArtifactReader.type().equalsIgnoreCase("jar")) {
            return
                TgzUtil
                    .findMatchingFiles(fdb, ".jar")
                    .map(s -> StringUtils.isNotBlank(s)
                                ? JavaAppDetail
                                    .from(detail)
                                    .jars(s)
                                    .springDependencies(javaArtifactReader.read(s).stream().collect(Collectors.joining("\n")))
                                    .build()
                                : detail);
        } else if (javaArtifactReader.type().equalsIgnoreCase("pom")) {
            return
                TgzUtil
                    .extractFileContent(fdb, "pom.xml")
                    .map(s -> StringUtils.isNotBlank(s)
                                ? JavaAppDetail
                                    .from(detail)
                                    .pomContents(s)
                                    .springDependencies(javaArtifactReader.read(s).stream().collect(Collectors.joining("\n")))
                                    .build()
                                : detail);
        } else {
            log.warn("Could not determine Spring dependencies");
            return Mono.just(detail);
        }
    }

    @Override
    public void onApplicationEvent(AppDetailRetrievedEvent event) {
        collect(List.copyOf(event.getDetail()));
    }

}
