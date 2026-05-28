package org.cftoolsuite.cfapp.controller;

import static org.mockito.Mockito.*;

import org.cftoolsuite.cfapp.domain.product.ProductMetrics;
import org.cftoolsuite.cfapp.service.ProductMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;

class ProductMetricsControllerTest extends ControllerTestBase {

    private ProductMetricsService service;
    private ProductMetricsController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        service = mock(ProductMetricsService.class);
        controller = new ProductMetricsController(service);
    }

    @Test
    void getProductMetrics_whenDataAvailable_returnsOk() {
        ProductMetrics metrics = ProductMetrics.builder().build();

        when(service.getProductMetrics()).thenReturn(Mono.just(metrics));

        assertOkBody(controller.getProductMetrics(), metrics);

        verify(service).getProductMetrics();
    }

    @Test
    void getProductMetrics_whenEmpty_returnsNotFound() {
        when(service.getProductMetrics()).thenReturn(Mono.empty());

        assertNotFound(controller.getProductMetrics());

        verify(service).getProductMetrics();
    }
}
