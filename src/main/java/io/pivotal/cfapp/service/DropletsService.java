package io.pivotal.cfapp.service;

import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.util.DropletProcessingCondition;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@Conditional(DropletProcessingCondition.class)
// @see https://v3-apidocs.cloudfoundry.org/version/3.118.0/index.html#download-droplet-bits
public class DropletsService {

    private final WebClient webClient;
    private final DefaultConnectionContext connectionContext;
    private final TokenProvider tokenProvider;
    private final PasSettings settings;

    @Autowired
    public DropletsService(
            WebClient webClient,
            DefaultConnectionContext connectionContext,
            TokenProvider tokenProvider,
            PasSettings settings) {
        this.webClient = webClient;
        this.connectionContext = connectionContext;
        this.tokenProvider = tokenProvider;
        this.settings = settings;
    }

    public Flux<DataBuffer> downloadDroplet(String id) {
        Assert.hasText(id, "Global unique identifier for droplet must not be blank or null!");
        final String uri =
            UriComponentsBuilder
                .newInstance()
                .scheme("https")
                .host(settings.getApiHost())
                .path("/v3/droplets/{id}/download")
                .buildAndExpand(id)
                .encode()
                .toUriString();
        log.trace("Attempting to download droplet with GET {}", uri);
        return
            tokenProvider
                .getToken(connectionContext)
                .flatMapMany(
                    t -> webClient
                        .get()
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, t)
                        .retrieve()
                        .bodyToFlux(DataBuffer.class));
    }

}
