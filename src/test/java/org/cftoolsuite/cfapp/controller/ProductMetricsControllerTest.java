package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.cftoolsuite.cfapp.domain.product.ProductMetrics;
import org.cftoolsuite.cfapp.service.ProductMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ProductMetricsControllerTest {

    private ProductMetricsService service;
    private ProductMetricsController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = mock(ProductMetricsService.class);
        controller = new ProductMetricsController(service);
    }

    @Test
    void getProductMetrics_whenDataAvailable_returnsOk() {
        ProductMetrics metrics = ProductMetrics.builder().build();

        when(service.getProductMetrics()).thenReturn(Mono.just(metrics));

        Mono<ResponseEntity<ProductMetrics>> result = controller.getProductMetrics();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(metrics, response.getBody());
                })
                .verifyComplete();

        verify(service).getProductMetrics();
    }

    @Test
    void getProductMetrics_whenEmpty_returnsNotFound() {
        when(service.getProductMetrics()).thenReturn(Mono.empty());

        Mono<ResponseEntity<ProductMetrics>> result = controller.getProductMetrics();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(service).getProductMetrics();
    }
}
