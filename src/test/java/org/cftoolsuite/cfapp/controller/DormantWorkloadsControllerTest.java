package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.domain.ServiceInstanceDetail;
import org.cftoolsuite.cfapp.domain.Workloads;
import org.cftoolsuite.cfapp.service.DormantWorkloadsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DormantWorkloadsControllerTest extends ControllerTestBase {

    private DormantWorkloadsService service;
    private DormantWorkloadsController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        service = mock(DormantWorkloadsService.class);
        controller = new DormantWorkloadsController(service, tkService);
    }

    @Test
    void getDormantWorkloads_whenDataAvailable_returnsOk() {
        AppDetail app = AppDetail.builder().appName("dormant-app").build();
        ServiceInstanceDetail si = ServiceInstanceDetail.builder().name("dormant-si").build();

        when(service.getDormantApplications(30)).thenReturn(Mono.just(Arrays.asList(app)));
        when(service.getDormantServiceInstances(30)).thenReturn(Mono.just(Arrays.asList(si)));
        mockTimeKeeper();

        Mono<ResponseEntity<Workloads>> result = controller.getDormantWorkloads(30);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().getApplications().size());
                    assertEquals(1, response.getBody().getServiceInstances().size());
                })
                .verifyComplete();
    }

    @Test
    void getDormantWorkloads_whenNoDormantServiceInstances_returnsNotFound() {
        AppDetail app = AppDetail.builder().appName("dormant-app").build();

        when(service.getDormantApplications(30)).thenReturn(Mono.just(Arrays.asList(app)));
        when(service.getDormantServiceInstances(30)).thenReturn(Mono.empty());

        assertNotFound(controller.getDormantWorkloads(30));
    }

    @Test
    void getDormantWorkloads_withDifferentDays() {
        AppDetail app = AppDetail.builder().appName("old-app").build();
        ServiceInstanceDetail si = ServiceInstanceDetail.builder().name("old-si").build();

        when(service.getDormantApplications(90)).thenReturn(Mono.just(Arrays.asList(app)));
        when(service.getDormantServiceInstances(90)).thenReturn(Mono.just(Arrays.asList(si)));
        mockTimeKeeper();

        assertOk(controller.getDormantWorkloads(90));
    }
}
