package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TimeKeeperControllerTest {

    private TimeKeeperService tkService;
    private TimeKeeperController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tkService = mock(TimeKeeperService.class);
        controller = new TimeKeeperController(tkService);
    }

    @Test
    void getCollectionTime_plainText_returnsOkWithTimestamp() {
        LocalDateTime collected = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

        when(tkService.findOne()).thenReturn(Mono.just(collected));

        Mono<ResponseEntity<?>> result = controller.getCollectionTime(MediaType.TEXT_PLAIN_VALUE);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("2024-01-15T10:30:00", response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
    }

    @Test
    void getCollectionTime_json_returnsOkWithJsonObject() {
        LocalDateTime collected = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

        when(tkService.findOne()).thenReturn(Mono.just(collected));

        Mono<ResponseEntity<?>> result = controller.getCollectionTime(MediaType.APPLICATION_JSON_VALUE);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertTrue(response.getBody() instanceof java.util.Map);
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, String> body = (java.util.Map<String, String>) response.getBody();
                    assertEquals("2024-01-15T10:30:00", body.get("timestamp"));
                })
                .verifyComplete();

        verify(tkService).findOne();
    }

    @Test
    void getCollectionTime_whenEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<?>> result = controller.getCollectionTime(MediaType.TEXT_PLAIN_VALUE);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
    }
}
