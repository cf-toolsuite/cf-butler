package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.cftoolsuite.cfapp.domain.product.PivnetCache;
import org.cftoolsuite.cfapp.domain.product.Products;
import org.cftoolsuite.cfapp.domain.product.Release;
import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PivnetControllerTest {

    private PivnetCache cache;
    private TimeKeeperService tkService;
    private PivnetController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cache = mock(PivnetCache.class);
        tkService = mock(TimeKeeperService.class);
        controller = new PivnetController(cache, tkService);
    }

    @Test
    void getLatestAvailableProductReleases_latestOption_returnsOk() {
        Release release = Release.builder().build();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-DateTime-Collected", "2024-01-15T10:30:00");

        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(cache.getLatestProductReleases()).thenReturn(Arrays.asList(release));

        Mono<ResponseEntity<List<Release>>> result = controller.getLatestAvailableProductReleases("latest");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().size());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(cache).getLatestProductReleases();
    }

    @Test
    void getLatestAvailableProductReleases_latestOption_whenTimeKeeperEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<List<Release>>> result = controller.getLatestAvailableProductReleases("latest");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
    }

    @Test
    void getLatestAvailableProductReleases_allOption_returnsOk() {
        Release release = Release.builder().build();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-DateTime-Collected", "2024-01-15T10:30:00");

        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(cache.getAllProductReleases()).thenReturn(Arrays.asList(release));

        Mono<ResponseEntity<List<Release>>> result = controller.getLatestAvailableProductReleases("all");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().size());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(cache).getAllProductReleases();
    }

    @Test
    void getLatestAvailableProductReleases_allOption_whenTimeKeeperEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<List<Release>>> result = controller.getLatestAvailableProductReleases("all");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
    }

    @Test
    void getLatestAvailableProductReleases_recentOption_returnsOk() {
        Release release = Release.builder().releaseDate(LocalDate.now()).build();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-DateTime-Collected", "2024-01-15T10:30:00");

        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(cache.getAllProductReleases()).thenReturn(Arrays.asList(release));

        Mono<ResponseEntity<List<Release>>> result = controller.getLatestAvailableProductReleases("recent");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(cache).getAllProductReleases();
    }

    @Test
    void getLatestAvailableProductReleases_invalidOption_returnsBadRequest() {
        Mono<ResponseEntity<List<Release>>> result = controller.getLatestAvailableProductReleases("invalid");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                })
                .verifyComplete();

        verifyNoInteractions(tkService);
        verifyNoInteractions(cache);
    }

    @Test
    void getProductList_whenDataAvailable_returnsOk() {
        Products products = Products.builder().build();

        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(cache.getProducts()).thenReturn(products);

        Mono<ResponseEntity<Products>> result = controller.getProductList();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(products, response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(cache).getProducts();
    }

    @Test
    void getProductList_whenTimeKeeperEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<Products>> result = controller.getProductList();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(cache);
    }
}
