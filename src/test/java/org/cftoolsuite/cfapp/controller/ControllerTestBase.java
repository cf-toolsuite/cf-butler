package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

abstract class ControllerTestBase {

    protected static final LocalDateTime COLLECTED = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

    protected TimeKeeperService tkService;

    void initMocks() {
        MockitoAnnotations.openMocks(this);
        tkService = mock(TimeKeeperService.class);
    }

    void mockTimeKeeper() {
        when(tkService.findOne()).thenReturn(Mono.just(COLLECTED));
    }

    void mockTimeKeeperEmpty() {
        when(tkService.findOne()).thenReturn(Mono.empty());
    }

    @SuppressWarnings("unchecked")
    void assertOk(Mono<?> result) {
        StepVerifier.create(result)
                .assertNext(r -> assertEquals(HttpStatus.OK, ((ResponseEntity<?>) r).getStatusCode()))
                .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    void assertNotFound(Mono<?> result) {
        StepVerifier.create(result)
                .assertNext(r -> assertEquals(HttpStatus.NOT_FOUND, ((ResponseEntity<?>) r).getStatusCode()))
                .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    void assertOkBody(Mono<?> result, Object expected) {
        StepVerifier.create(result)
                .assertNext(r -> {
                    ResponseEntity<?> resp = (ResponseEntity<?>) r;
                    assertEquals(HttpStatus.OK, resp.getStatusCode());
                    assertEquals(expected, resp.getBody());
                })
                .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    void assertBadRequest(Mono<?> result) {
        StepVerifier.create(result)
                .assertNext(r -> assertEquals(HttpStatus.BAD_REQUEST, ((ResponseEntity<?>) r).getStatusCode()))
                .verifyComplete();
    }
}
