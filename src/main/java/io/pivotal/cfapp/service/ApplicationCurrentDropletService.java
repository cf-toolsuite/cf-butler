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
import reactor.core.publisher.Mono;


@Service
// @see https://v3-apidocs.cloudfoundry.org/version/3.107.0/index.html#get-current-droplet
public class ApplicationCurrentDropletService {

    private final WebClient webClient;
    private final DefaultConnectionContext connectionContext;
    private final TokenProvider tokenProvider;
    private final PasSettings settings;

    @Autowired
    public ApplicationCurrentDropletService(
            WebClient webClient,
            DefaultConnectionContext connectionContext,
            TokenProvider tokenProvider,
            PasSettings settings) {
        this.webClient = webClient;
        this.connectionContext = connectionContext;
        this.tokenProvider = tokenProvider;
        this.settings = settings;
    }

    public Mono<String> getCurrentDroplet(String guid) {
        Assert.hasText(guid, "Global unique identifier for application instance must not be blank or null!");
        final String uri =
            UriComponentsBuilder
                .newInstance()
                .scheme("https")
                .host(settings.getApiHost())
                .path("/v3/apps/{guid}/droplets/current")
                .buildAndExpand(guid)
                .encode()
                .toUriString();
        return
            tokenProvider
                .getToken(connectionContext)
                .flatMap(t ->
                    webClient
                        .get()
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, t)
                        .retrieve()
                        .bodyToMono(String.class)
                );
    }

}
