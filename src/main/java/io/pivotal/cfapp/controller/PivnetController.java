package io.pivotal.cfapp.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.product.PivnetCache;
import io.pivotal.cfapp.domain.product.Products;
import io.pivotal.cfapp.domain.product.Release;
import io.pivotal.cfapp.service.TimeKeeperService;
import io.pivotal.cfapp.service.TkServiceUtil;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "pivnet.enabled", havingValue = "true")
public class PivnetController {

    private final PivnetCache cache;
    private final TkServiceUtil util;

    @Autowired
    public PivnetController(
            PivnetCache cache,
            TimeKeeperService tkService) {
        this.cache = cache;
        this.util = new TkServiceUtil(tkService);
    }

    @GetMapping("/store/product/releases")
    public Mono<ResponseEntity<List<Release>>> getLatestAvailableProductReleases(
            @RequestParam(name = "q", defaultValue = "latest") String option) {
        if (option.equalsIgnoreCase("latest")) {
            return util.getHeaders()
                    .map(h -> new ResponseEntity<>(cache.getLatestProductReleases(), h, HttpStatus.OK))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        } else if (option.equalsIgnoreCase("all")) {
            return util.getHeaders()
                    .map(h -> new ResponseEntity<>(cache.getAllProductReleases(), h, HttpStatus.OK))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        } else if (option.equalsIgnoreCase("recent")) {
            List<Release> recentReleases = cache
                    .getAllProductReleases()
                    .stream()
                    .filter(r -> r.getReleaseDate().isAfter(LocalDate.now().minusDays(7)))
                    .collect(Collectors.toList());
            return util.getHeaders()
                    .map(h -> new ResponseEntity<>(recentReleases, h, HttpStatus.OK))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        } else {
            return Mono.just(ResponseEntity.badRequest().build());
        }
    }

    @GetMapping("/store/product/catalog")
    public Mono<ResponseEntity<Products>> getProductList() {
        return util.getHeaders()
                .map(h -> new ResponseEntity<>(cache.getProducts(), h, HttpStatus.OK))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
