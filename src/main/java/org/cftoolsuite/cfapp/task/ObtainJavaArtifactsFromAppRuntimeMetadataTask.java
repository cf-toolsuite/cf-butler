package org.cftoolsuite.cfapp.task;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.domain.JavaAppDetail;
import org.cftoolsuite.cfapp.event.AppDetailRetrievedEvent;
import org.cftoolsuite.cfapp.service.JavaAppDetailService;
import org.cftoolsuite.cfapp.service.JavaArtifactRuntimeMetadataRetrievalService;
import org.cftoolsuite.cfapp.util.JavaArtifactReader;
import org.cloudfoundry.client.v3.applications.GetApplicationCurrentDropletRequest;
import org.cloudfoundry.client.v3.applications.GetApplicationCurrentDropletResponse;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "java.artifacts.fetch", name= "mode", havingValue="obtain-jars-from-runtime-metadata")
public class ObtainJavaArtifactsFromAppRuntimeMetadataTask implements ApplicationListener<AppDetailRetrievedEvent> {

    private DefaultCloudFoundryOperations opsClient;
    private JavaArtifactRuntimeMetadataRetrievalService artifactService;
    private JavaAppDetailService jadService;
    private JavaArtifactReader javaArtifactReader;

    @Autowired
    public ObtainJavaArtifactsFromAppRuntimeMetadataTask(
            DefaultCloudFoundryOperations opsClient,
            JavaArtifactRuntimeMetadataRetrievalService artifactService,
            JavaAppDetailService jadService,
            JavaArtifactReader javaArtifactReader,
            Environment env
            ) {
        this.opsClient = opsClient;
        this.artifactService = artifactService;
        this.jadService = jadService;
        this.javaArtifactReader = javaArtifactReader;
    }

    public void collect(List<AppDetail> detail) {
        log.info("ObtainJavaArtifactsFromAppRuntimeMetadataTask started");
        jadService
            .deleteAll()
            .thenMany(Flux.fromIterable(detail))
            .filter(ad -> StringUtils.isNotBlank(ad.getBuildpack()) && ad.getBuildpack().contains("java") && ad.getRequestedState().equalsIgnoreCase("started"))
            .flatMap(ad -> associateDropletWithApplication(ad))
            .flatMap(sd -> ascertainSpringDependencies(sd))
            .flatMap(jadService::save)
            .thenMany(jadService.findAll())
            .collectList()
            .subscribe(
                result -> {
                    log.info("ObtainJavaArtifactsFromAppRuntimeMetadataTask completed. {} running applications processed.", result.size());
                },
                error -> {
                    log.error("ObtainJavaArtifactsFromAppRuntimeMetadataTask terminated with error", error);
                }
            );
    }

    private Mono<Tuple2<AppDetail, JavaAppDetail>> associateDropletWithApplication(AppDetail detail) {
        log.trace("Attempting to fetch droplet id for {}/{}/{} whose state is {}", detail.getOrganization(), detail.getSpace(), detail.getAppName(), detail.getRequestedState());
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
                    .map(dr -> Tuples.of(detail, JavaAppDetail.from(detail).dropletId(dr.getId()).build()));
    }

    private Mono<JavaAppDetail> ascertainSpringDependencies(Tuple2<AppDetail, JavaAppDetail> tuple) {
        if (javaArtifactReader.mode().equalsIgnoreCase("list-jars")) {
            return
                artifactService
                    .obtainRuntimeMetadata(tuple.getT1())
                    .map(jad -> JavaAppDetail
                                    .from(tuple.getT2())
                                    .jars(jad.getJars())
                                    .springDependencies(javaArtifactReader.read(jad.getJars()).stream().collect(Collectors.joining("\n")))
                                    .build())
                    .onErrorResume(e -> {
                        log.error(String.format("Trouble ascertaining Spring dependencies for %s/%s/%s", tuple.getT2().getOrganization(), tuple.getT2().getSpace(), tuple.getT2().getAppName()), e);
                        return Mono.just(JavaAppDetail.from(tuple.getT2()).build());
                    });
        } else {
            log.warn("Not configured to ascertain Spring dependencies");
            return Mono.just(JavaAppDetail.from(tuple.getT2()).build());
        }
    }

    @Override
    public void onApplicationEvent(AppDetailRetrievedEvent event) {
        collect(List.copyOf(event.getDetail()));
    }

}
