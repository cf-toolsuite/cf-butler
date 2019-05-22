package io.pivotal.cfapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.client.PivnetClient;
import io.pivotal.cfapp.domain.product.Release;
import reactor.core.publisher.Mono;

@RestController
public class PivnetController {

    private final PivnetClient client;

    @Autowired
    public PivnetController(PivnetClient client) {
        this.client = client;
    }

    @GetMapping("/product/releases")
    public Mono<ResponseEntity<List<Release>>> getLatestAvailableProductReleases(@RequestParam("latest") boolean latest) {
        return client
                .getLatestProductReleases()
                .collectList()
                    .map(l -> ResponseEntity.ok(l))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}