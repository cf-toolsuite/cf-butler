package org.cftoolsuite.cfapp.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.domain.JavaAppDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "java.artifacts.fetch", name= "mode", havingValue="obtain-jars-from-runtime-metadata")
public class JavaArtifactRuntimeMetadataRetrievalService {

    private final WebClient webClient;

    @Autowired
    public JavaArtifactRuntimeMetadataRetrievalService(
            WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<JavaAppDetail> obtainRuntimeMetadata(AppDetail detail) {
        Assert.state(
            detail != null && !Collections.isEmpty(detail.getUrls()),
            String.format("A route must be defined for %s/%s/%s in order to obtain runtime metadata",
                detail.getOrganization(), detail.getSpace(), detail.getAppName()));
        String route = detail.getUrls().get(0);
        final String uri =
            UriComponentsBuilder
                .newInstance()
                .scheme("https")
                .host(route)
                .path("/actuator/jars")
                .encode()
                .toUriString();
        log.trace("Attempting to fetch runtime metadata for {}/{}/{} with GET {}", detail.getOrganization(), detail.getSpace(), detail.getAppName(), route);
        return
            webClient
                .get()
                .uri(uri)
                .retrieve()
                .onStatus(
                    status -> status.isError(),
                    response -> Mono.error(new RuntimeException("Client or Server error")))
                .bodyToMono(new ParameterizedTypeReference<Set<String>>(){})
                .map(set -> set.stream().collect(Collectors.joining(System.getProperty("line.separator"))))
                .map(jars -> JavaAppDetail.from(detail).jars(jars).build())
                .onErrorResume(e -> {
                    log.error("Error fetching runtime metadata for {}/{}/{}: {}", detail.getOrganization(), detail.getSpace(), detail.getAppName(), e.getMessage());
                    return Mono.just(JavaAppDetail.from(detail).build());
                });
    }

}
