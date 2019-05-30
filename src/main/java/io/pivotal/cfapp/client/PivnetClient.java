package io.pivotal.cfapp.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.pivotal.cfapp.config.PivnetSettings;
import io.pivotal.cfapp.domain.product.Products;
import io.pivotal.cfapp.domain.product.Release;
import io.pivotal.cfapp.domain.product.Releases;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PivnetClient {

    private static final String BASE_URL = "https://network.pivotal.io/api";
    private final PivnetSettings settings;
    private final WebClient client;

    @Autowired
    public PivnetClient(PivnetSettings settings, WebClient client) {
        this.settings = settings;
        this.client = client;
    }

    public Mono<Products> getProducts() {
        String uri = String.format("%s%s", BASE_URL, "/v2/products");
        return client
                .get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, settings.getApiToken())
                    .retrieve()
                    .bodyToMono(Products.class);
    }

    public Flux<Release> getProductReleases(String slug) {
        String uri = String.format("%s%s%s%s", BASE_URL, "/v2/products/", slug, "/releases");
        return client
                .get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, settings.getApiToken())
                    .retrieve()
                    .bodyToMono(Releases.class)
                    .flatMapMany(r -> Flux.fromIterable(r.getReleases()));
    }

    public Mono<Release> getLatestProductRelease(String slug) {
        return getProductReleases(slug)
                .next()
                .onErrorContinue(
                    (ex, data) -> log.warn("Problem obtaining releases for product [{}] .", slug, ex));
    }

    public Flux<Release> getLatestProductReleases() {
        return getProducts()
                .flatMapMany(list -> Flux.fromIterable(list.getProducts()))
                .flatMap(p -> getLatestProductRelease(p.getSlug()));
    }

    public Flux<Release> getAllProductReleases() {
        return getProducts()
                .flatMapMany(list -> Flux.fromIterable(list.getProducts()))
                .flatMap(l -> getProductReleases(l.getSlug()))
                .onErrorContinue(
                    (ex, data) -> log.warn("Problem obtaining releases for product [{}] .", data, ex));
    }
}