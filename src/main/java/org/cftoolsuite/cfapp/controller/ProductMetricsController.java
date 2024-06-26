package org.cftoolsuite.cfapp.controller;

import org.cftoolsuite.cfapp.domain.product.ProductMetrics;
import org.cftoolsuite.cfapp.service.ProductMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@ConditionalOnExpression(
        "${om.enabled:false} and ${pivnet.enabled:false}"
        )
public class ProductMetricsController {

    private final ProductMetricsService service;

    @Autowired
    public ProductMetricsController(ProductMetricsService service) {
        this.service = service;
    }

    @GetMapping("/products/metrics")
    public Mono<ResponseEntity<ProductMetrics>> getProductMetrics() {
        return service
                .getProductMetrics()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
