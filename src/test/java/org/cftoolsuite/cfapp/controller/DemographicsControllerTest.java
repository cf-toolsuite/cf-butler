package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.cftoolsuite.cfapp.domain.Demographics;
import org.cftoolsuite.cfapp.service.DemographicsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import org.springframework.http.HttpStatus;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DemographicsControllerTest extends ControllerTestBase {

    private DemographicsService demoService;
    private DemographicsController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        demoService = mock(DemographicsService.class);
        controller = new DemographicsController(demoService, tkService);
    }

    @Test
    void getDemographics_whenDataAvailable_returnsOk() {
        Demographics demographics = Demographics.builder().build();

        mockTimeKeeper();
        when(demoService.getDemographics()).thenReturn(Mono.just(demographics));

        Mono<ResponseEntity<Demographics>> result = controller.getDemographics();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(demographics, response.getBody());
                    assertFalse(response.getHeaders().isEmpty());
                })
                .verifyComplete();

        verify(demoService).getDemographics();
    }

    @Test
    void getDemographics_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.getDemographics());

        verifyNoInteractions(demoService);
    }
}
