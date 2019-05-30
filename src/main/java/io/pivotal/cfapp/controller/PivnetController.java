package io.pivotal.cfapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.product.PivnetCache;
import io.pivotal.cfapp.domain.product.Products;
import io.pivotal.cfapp.domain.product.Release;
import io.pivotal.cfapp.service.TkService;
import io.pivotal.cfapp.service.TkServiceUtil;
import reactor.core.publisher.Mono;

@RestController
public class PivnetController {

    private final PivnetCache cache;
    private final TkServiceUtil util;

    @Autowired
    public PivnetController(
        PivnetCache cache,
        TkService tkService) {
        this.cache = cache;
        this.util = new TkServiceUtil(tkService);
    }

    @GetMapping("/store/product/catalog")
    public Mono<ResponseEntity<Products>> getProductList() {
        return util.getHeaders()
                .map(h -> new ResponseEntity<>(cache.getProducts(), h, HttpStatus.OK))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/store/product/releases")
    public Mono<ResponseEntity<List<Release>>> getLatestAvailableProductReleases(
        @RequestParam(name = "latest", defaultValue = "true") boolean latest) {
        return util.getHeaders()
                .map(h -> new ResponseEntity<>(cache.getLatestProductReleases(), h, HttpStatus.OK))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}