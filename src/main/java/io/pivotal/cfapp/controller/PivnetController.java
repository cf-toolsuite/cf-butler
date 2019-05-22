package io.pivotal.cfapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.product.PivnetCache;
import io.pivotal.cfapp.domain.product.Products;
import io.pivotal.cfapp.domain.product.Release;

@RestController
public class PivnetController {

    private final PivnetCache cache;

    @Autowired
    public PivnetController(PivnetCache cache) {
        this.cache = cache;
    }

    @GetMapping("/product/list")
    public ResponseEntity<Products> getProductList() {
        return ResponseEntity.ok(cache.getProducts());
    }

    @GetMapping("/product/releases")
    public ResponseEntity<List<Release>> getLatestAvailableProductReleases(@RequestParam("latest") boolean latest) {
        return ResponseEntity.ok(cache.getLatestProductReleases());
    }

}