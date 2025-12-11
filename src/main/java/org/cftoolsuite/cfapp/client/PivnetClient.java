package org.cftoolsuite.cfapp.client;

import org.cftoolsuite.cfapp.domain.product.Products;
import org.cftoolsuite.cfapp.domain.product.Release;
import org.cftoolsuite.cfapp.domain.product.Releases;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@ConditionalOnProperty(name = "pivnet.enabled", havingValue = "true")
public class PivnetClient {

    private static final String BASE_URL = "https://network.tanzu.vmware.com/api";
    private final WebClient client;

    @Autowired
    public PivnetClient(WebClient client) {
        this.client = client;
    }

    public Flux<Release> getAllProductReleases() {
        return getProducts()
                .flatMapMany(list -> Flux.fromIterable(list.getProducts()))
                .flatMap(l -> getProductReleases(l.getSlug()))
                .onErrorContinue(
                        (ex, data) -> log.warn("Problem obtaining releases for product [{}].", data, ex));
    }

    public Mono<Release> getLatestProductRelease(String slug) {
        return getProductReleases(slug)
                .next()
                .onErrorContinue(
                        (ex, data) -> log.warn("Problem obtaining releases for product [{}].", slug, ex));
    }

    public Flux<Release> getLatestProductReleases() {
        return getProducts()
                .flatMapMany(list -> Flux.fromIterable(list.getProducts()))
                .flatMap(p -> getLatestProductRelease(p.getSlug()));
    }

    public Flux<Release> getProductReleases(String slug) {
        String uri = String.format("%s%s%s%s", BASE_URL, "/v2/products/", slug, "/releases");
        return client
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Releases.class)
                .flatMapMany(r -> Flux.fromIterable(r.getReleases()));
    }

    public Mono<Products> getProducts() {
        String uri = String.format("%s%s", BASE_URL, "/v2/products");
        return client
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Products.class);
    }
}
