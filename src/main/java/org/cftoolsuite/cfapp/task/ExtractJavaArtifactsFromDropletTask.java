package org.cftoolsuite.cfapp.task;

import java.io.IOException;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.domain.JavaAppDetail;
import org.cftoolsuite.cfapp.event.AppDetailRetrievedEvent;
import org.cftoolsuite.cfapp.service.DropletsService;
import org.cftoolsuite.cfapp.service.JavaAppDetailService;
import org.cftoolsuite.cfapp.util.DropletProcessingCondition;
import org.cftoolsuite.cfapp.util.JarManifestUtil;
import org.cftoolsuite.cfapp.util.JavaArtifactReader;
import org.cftoolsuite.cfapp.util.TgzUtil;
import org.cloudfoundry.client.v3.applications.GetApplicationCurrentDropletRequest;
import org.cloudfoundry.client.v3.applications.GetApplicationCurrentDropletResponse;
import org.cloudfoundry.client.v3.droplets.DropletResource;
import org.cloudfoundry.client.v3.droplets.DropletState;
import org.cloudfoundry.client.v3.droplets.ListDropletsRequest;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Conditional(DropletProcessingCondition.class)
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
            Environment env) {
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
                .flatMap(jad -> associateDropletWithApplication(jad))
                .flatMap(sd -> ascertainSpringDependencies(sd))
                .flatMap(jadService::save)
                .thenMany(jadService.findAll())
                .collectList()
                .subscribe(
                        result -> {
                            log.info("ExtractJavaArtifactsFromDropletTask completed. {} droplets processed.",
                                    result.size());
                        },
                        error -> {
                            log.error("ExtractJavaArtifactsFromDropletTask terminated with error", error);
                        });
    }

    private Mono<JavaAppDetail> associateDropletWithApplication(AppDetail detail) {
        log.trace("Attempting to fetch droplet id for {}/{}/{} whose state is {}", detail.getOrganization(),
                detail.getSpace(), detail.getAppName(), detail.getRequestedState());
        if (detail.getRequestedState().equalsIgnoreCase("started")) {
            Mono<GetApplicationCurrentDropletResponse> currentResponse = DefaultCloudFoundryOperations.builder()
                    .from(opsClient)
                    .organization(detail.getOrganization())
                    .space(detail.getSpace())
                    .build()
                    .getCloudFoundryClient()
                    .applicationsV3()
                    .getCurrentDroplet(
                            GetApplicationCurrentDropletRequest.builder().applicationId(detail.getAppId()).build());
            return currentResponse
                    .map(dr -> JavaAppDetail.from(detail).dropletId(dr.getId()).build());
        } else if (detail.getRequestedState().equalsIgnoreCase("stopped")) {
            Mono<DropletResource> stagedResponse = DefaultCloudFoundryOperations.builder()
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
                    .map(dr -> JavaAppDetail.from(detail).dropletId(dr.getId()).build());
        } else {
            log.trace("No droplet found for {}/{}/{}", detail.getOrganization(), detail.getSpace(),
                    detail.getAppName());
            return Mono.just(JavaAppDetail.from(detail).build());
        }
    }

    private Mono<JavaAppDetail> ascertainSpringDependencies(JavaAppDetail detail) {
        Flux<DataBuffer> fdb = dropletsService.downloadDroplet(detail.getDropletId());

        Mono<String> manifestContentMono =
            TgzUtil
                .extractFileContent(fdb, "META-INF/MANIFEST.MF")
                .defaultIfEmpty("");

        if (javaArtifactReader.mode().equalsIgnoreCase("list-jars")) {
            return manifestContentMono.flatMap(manifestContent -> {
                Integer buildJdkSpec = extractBuildJdkSpec(manifestContent);

                return TgzUtil.findMatchingFiles(fdb, ".jar")
                        .map(s -> StringUtils.isNotBlank(s)
                                ? JavaAppDetail
                                        .from(detail)
                                        .jars(s)
                                        .buildJdkSpec(buildJdkSpec)
                                        .springDependencies(
                                                javaArtifactReader.read(s).stream().collect(Collectors.joining("\n")))
                                        .build()
                                : detail)
                        .onErrorResume(e -> {
                            log.error(String.format("Trouble ascertaining Spring dependencies for %s/%s/%s",
                                    detail.getOrganization(), detail.getSpace(), detail.getAppName()), e);
                            return Mono.just(detail);
                        });
            });
        } else if (javaArtifactReader.mode().equalsIgnoreCase("unpack-pom-contents-in-droplet")) {
            return manifestContentMono.flatMap(manifestContent -> {
                Integer buildJdkSpec = extractBuildJdkSpec(manifestContent);

                return TgzUtil.extractFileContent(fdb, "pom.xml")
                        .map(s -> StringUtils.isNotBlank(s)
                                ? JavaAppDetail
                                        .from(detail)
                                        .pomContents(s)
                                        .buildJdkSpec(buildJdkSpec)
                                        .springDependencies(
                                                javaArtifactReader.read(s).stream().collect(Collectors.joining("\n")))
                                        .build()
                                : detail)
                        .onErrorResume(e -> {
                            log.error(String.format("Trouble ascertaining Spring dependencies for %s/%s/%s",
                                    detail.getOrganization(), detail.getSpace(), detail.getAppName()), e);
                            return Mono.just(detail);
                        });
            });
        } else {
            log.warn("Not configured to ascertain Spring dependencies");
            return Mono.just(detail);
        }
    }

    private Integer extractBuildJdkSpec(String manifestContent) {
        if (manifestContent.isEmpty()) {
            return null;
        }
        try {
            String buildJdkSpecStr = JarManifestUtil.obtainAttributeValue(manifestContent, "Build-Jdk-Spec");
            return buildJdkSpecStr != null ? Integer.valueOf(buildJdkSpecStr) : null;
        } catch (IOException e) {
            log.error("Error reading MANIFEST.MF", e);
            return null;
        }
    }

    @Override
    public void onApplicationEvent(AppDetailRetrievedEvent event) {
        collect(List.copyOf(event.getDetail()));
    }

}
