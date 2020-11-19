package io.pivotal.cfapp.service;

import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.Metadata;
import io.pivotal.cfapp.domain.Resource;
import io.pivotal.cfapp.domain.Resources;
import io.pivotal.cfapp.domain.ResourceType;
import reactor.core.publisher.Mono;

@Service
public class ResourceMetadataService {

    private final WebClient webClient;
    private final DefaultConnectionContext connectionContext;
    private final TokenProvider tokenProvider;
    private final PasSettings settings;

    @Autowired
    public ResourceMetadataService(
            WebClient webClient,
            DefaultConnectionContext connectionContext,
            TokenProvider tokenProvider,
            PasSettings settings) {
        this.webClient = webClient;
        this.connectionContext = connectionContext;
        this.tokenProvider = tokenProvider;
        this.settings = settings;
    }
    private Mono<String> getOauthToken() {
        tokenProvider.invalidate(connectionContext);
        return tokenProvider.getToken(connectionContext);
    }
    public Mono<Resources> getResources(String type) {
        ResourceType rt = ResourceType.from(type);
        final String uri =
            UriComponentsBuilder
                .newInstance()
                    .scheme("https")
                    .host(settings.getApiHost())
                    .path("/v3/{type}")
                    .buildAndExpand(rt.getId())
                    .encode()
                    .toUriString();
        return
            getOauthToken()
                .flatMap(t -> webClient
                                .get()
                                    .uri(uri)
                                    .header(HttpHeaders.AUTHORIZATION, t)
                                        .retrieve()
                                            .bodyToMono(Resources.class));
    }


    public Mono<Resources> getResources(String type, String labelSelector, Integer page, Integer perPage) {
        ResourceType rt = ResourceType.from(type);
        final String uri =
            UriComponentsBuilder
                .newInstance()
                    .scheme("https")
                    .host(settings.getApiHost())
                    .path("/v3/{type}")
                    .queryParam("label_selector", labelSelector)
                    .queryParam("page", "{page}")
                    .queryParam("per_page", "{perPage}")
                    .buildAndExpand(rt.getId(),page ==null ? 1:page ,perPage == null ? 50:perPage)
                    .toUriString();
        return
            getOauthToken()
                .flatMap(t -> webClient
                                .get()
                                    .uri(uri)
                                    .header(HttpHeaders.AUTHORIZATION, t)
                                        .retrieve()
                                            .bodyToMono(Resources.class));
    }

    public Mono<Resource> getResource(String type, String id) {
        Assert.hasText(id, "Global unique identifier for application must not be blank or null!");
                ResourceType rt = ResourceType.from(type);
                final String uri =
                        UriComponentsBuilder
                        .newInstance()
                        .scheme("https")
                        .host(settings.getApiHost())
                        .path("/v3/{type}/{guid}")
                        .buildAndExpand(rt.getId(), id)
                        .encode()
                        .toUriString();
                return
                        getOauthToken()
                        .flatMap(t -> webClient
                                .get()
                                .uri(uri)
                                .header(HttpHeaders.AUTHORIZATION, t)
                                .retrieve()
                                .bodyToMono(Resource.class));
    }

    public Mono<Metadata> updateResource(String type, String id, Metadata metadata) {
        ResourceType rt = ResourceType.from(type);
        if (metadata.isValid()) {
            final String uri =
                    UriComponentsBuilder
                    .newInstance()
                    .scheme("https")
                    .host(settings.getApiHost())
                    .path("/v3/{type}/{id}")
                    .buildAndExpand(rt.getId(), id)
                    .encode()
                    .toUriString();
            return
                    getOauthToken()
                    .flatMap(t -> webClient
                            .patch()
                            .uri(uri)
                            .bodyValue(metadata)
                            .header(HttpHeaders.AUTHORIZATION, t)
                            .retrieve()
                            .bodyToMono(Metadata.class));
        } else {
            return Mono.error(new IllegalArgumentException(String.format("Invalid metadata %s", metadata.toString())));
        }
    }

}
