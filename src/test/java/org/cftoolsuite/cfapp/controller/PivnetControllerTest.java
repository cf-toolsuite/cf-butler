package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.cftoolsuite.cfapp.domain.product.PivnetCache;
import org.cftoolsuite.cfapp.domain.product.Products;
import org.cftoolsuite.cfapp.domain.product.Release;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PivnetControllerTest extends ControllerTestBase {

    private PivnetCache cache;
    private PivnetController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        cache = mock(PivnetCache.class);
        controller = new PivnetController(cache, tkService);
    }

    @Test
    void getLatestAvailableProductReleases_latestOption_returnsOk() {
        Release release = Release.builder().build();

        mockTimeKeeper();
        when(cache.getLatestProductReleases()).thenReturn(Arrays.asList(release));

        Mono<ResponseEntity<List<Release>>> result = controller.getLatestAvailableProductReleases("latest");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().size());
                })
                .verifyComplete();
    }

    @Test
    void getLatestAvailableProductReleases_latestOption_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.getLatestAvailableProductReleases("latest"));
    }

    @Test
    void getLatestAvailableProductReleases_allOption_returnsOk() {
        Release release = Release.builder().build();

        mockTimeKeeper();
        when(cache.getAllProductReleases()).thenReturn(Arrays.asList(release));

        Mono<ResponseEntity<List<Release>>> result = controller.getLatestAvailableProductReleases("all");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().size());
                })
                .verifyComplete();
    }

    @Test
    void getLatestAvailableProductReleases_allOption_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.getLatestAvailableProductReleases("all"));
    }

    @Test
    void getLatestAvailableProductReleases_recentOption_returnsOk() {
        Release release = Release.builder().releaseDate(LocalDate.now()).build();

        mockTimeKeeper();
        when(cache.getAllProductReleases()).thenReturn(Arrays.asList(release));

        Mono<ResponseEntity<List<Release>>> result = controller.getLatestAvailableProductReleases("recent");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void getLatestAvailableProductReleases_invalidOption_returnsBadRequest() {
        assertBadRequest(controller.getLatestAvailableProductReleases("invalid"));
    }

    @Test
    void getProductList_whenDataAvailable_returnsOk() {
        Products products = Products.builder().build();

        mockTimeKeeper();
        when(cache.getProducts()).thenReturn(products);

        assertOkBody(controller.getProductList(), products);
    }

    @Test
    void getProductList_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.getProductList());
    }
}
