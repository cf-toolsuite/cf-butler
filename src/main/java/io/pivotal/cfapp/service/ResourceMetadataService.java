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
import io.pivotal.cfapp.domain.Resource;
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

    public Mono<Resource> getResource(String id) {
        Assert.hasText(id, "Global unique identifier for application must not be blank or null!");
        final String uri =
            UriComponentsBuilder
                .newInstance()
                    .scheme("https")
                    .host(settings.getApiHost())
                    .path("/v3/apps/{guid}")
                    .buildAndExpand(id)
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

    public Mono<Resource> patchResource(Resource resource) {
        Assert.hasText(resource.getGuid(), "Global unique identifier for application must not be blank or null!");
        final String uri =
            UriComponentsBuilder
                .newInstance()
                    .scheme("https")
                    .host(settings.getApiHost())
                    .path("/v3/apps/{guid}")
                    .buildAndExpand(resource.getGuid())
                    .encode()
                    .toUriString();
        return
            getOauthToken()
                .flatMap(t -> webClient
                                .patch()
                                    .uri(uri)
                                    .body(resource)
                                    .header(HttpHeaders.AUTHORIZATION, t)
                                        .retrieve()
                                            .bodyToMono(Resource.class));
    }

    private Mono<String> getOauthToken() {
        tokenProvider.invalidate(connectionContext);
        return tokenProvider.getToken(connectionContext);
    }

}