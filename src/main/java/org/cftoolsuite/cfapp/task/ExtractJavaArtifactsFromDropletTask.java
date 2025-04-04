package org.cftoolsuite.cfapp.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.domain.JavaAppDetail;
import org.cftoolsuite.cfapp.event.AppDetailRetrievedEvent;
import org.cftoolsuite.cfapp.service.DropletsService;
import org.cftoolsuite.cfapp.service.JavaAppDetailService;
import org.cftoolsuite.cfapp.util.DropletProcessingCondition;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@Conditional(DropletProcessingCondition.class)
public class ExtractJavaArtifactsFromDropletTask implements ApplicationListener<AppDetailRetrievedEvent> {

    private final DefaultCloudFoundryOperations opsClient;
    private final DropletsService dropletsService;
    private final JavaAppDetailService jadService;
    private final JavaArtifactReader javaArtifactReader;

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
                .flatMap(this::associateDropletWithApplication)
                .flatMap(this::ascertainSpringDependencies)
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

        if (javaArtifactReader.mode().equalsIgnoreCase("list-jars")) {
            return TgzUtil.processArchive(fdb, new String[]{".jar", "META-INF/MANIFEST.MF"})
                    .map(result -> {
                        String jars = result.getJarFilenamesAsString();
                        Integer buildJdkSpec = result.buildJdkSpec();

                        if (StringUtils.isNotBlank(jars)) {
                            return JavaAppDetail
                                    .from(detail)
                                    .jars(jars)
                                    .buildJdkSpec(buildJdkSpec)
                                    .springDependencies(
                                            String.join("\n", javaArtifactReader.read(jars)))
                                    .build();
                        } else {
                            return detail;
                        }
                    })
                    .onErrorResume(e -> {
                        log.error(String.format("Trouble ascertaining Spring dependencies for %s/%s/%s",
                                detail.getOrganization(), detail.getSpace(), detail.getAppName()), e);
                        return Mono.just(detail);
                    });
        } else if (javaArtifactReader.mode().equalsIgnoreCase("unpack-pom-contents-in-droplet")) {
            return TgzUtil.processArchive(fdb, new String[]{"pom.xml", "META-INF/MANIFEST.MF"})
                    .map(result -> {
                        String pomContents = result.pomContent();
                        Integer buildJdkSpec = result.buildJdkSpec();

                        if (StringUtils.isNotBlank(pomContents)) {
                            return JavaAppDetail
                                    .from(detail)
                                    .pomContents(pomContents)
                                    .buildJdkSpec(buildJdkSpec)
                                    .springDependencies(
                                            String.join("\n", javaArtifactReader.read(pomContents)))
                                    .build();
                        } else {
                            return detail;
                        }
                    })
                    .onErrorResume(e -> {
                        log.error(String.format("Trouble ascertaining Spring dependencies for %s/%s/%s",
                                detail.getOrganization(), detail.getSpace(), detail.getAppName()), e);
                        return Mono.just(detail);
                    });
        } else {
            log.warn("Not configured to ascertain Spring dependencies");
            return Mono.just(detail);
        }
    }

    @Override
    public void onApplicationEvent(AppDetailRetrievedEvent event) {
        collect(List.copyOf(event.getDetail()));
    }
}