package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.cftoolsuite.cfapp.domain.Demographics;
import org.cftoolsuite.cfapp.service.DemographicsService;
import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DemographicsControllerTest {

    private DemographicsService demoService;
    private TimeKeeperService tkService;
    private DemographicsController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        demoService = mock(DemographicsService.class);
        tkService = mock(TimeKeeperService.class);
        controller = new DemographicsController(demoService, tkService);
    }

    @Test
    void getDemographics_whenDataAvailable_returnsOk() {
        Demographics demographics = Demographics.builder().build();

        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(demoService.getDemographics()).thenReturn(Mono.just(demographics));

        Mono<ResponseEntity<Demographics>> result = controller.getDemographics();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(demographics, response.getBody());
                    assertFalse(response.getHeaders().isEmpty());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(demoService).getDemographics();
    }

    @Test
    void getDemographics_whenTimeKeeperEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<Demographics>> result = controller.getDemographics();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(demoService);
    }
}
