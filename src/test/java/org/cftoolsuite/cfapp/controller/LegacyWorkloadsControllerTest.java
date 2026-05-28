package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.domain.AppRelationship;
import org.cftoolsuite.cfapp.domain.Workloads;
import org.cftoolsuite.cfapp.domain.WorkloadsFilter;
import org.cftoolsuite.cfapp.service.LegacyWorkloadsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class LegacyWorkloadsControllerTest extends ControllerTestBase {

    private LegacyWorkloadsService service;
    private LegacyWorkloadsController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        service = mock(LegacyWorkloadsService.class);
        controller = new LegacyWorkloadsController(service, tkService);
    }

    @Test
    void getLegacyWorkloads_whenDataAvailable_returnsOk() {
        AppDetail app = AppDetail.builder().appName("legacy-app").build();
        AppRelationship rel = AppRelationship.builder()
                .appName("legacy-app")
                .build();

        when(service.getLegacyApplications(any(WorkloadsFilter.class)))
                .thenReturn(Mono.just(Arrays.asList(app)));
        when(service.getLegacyApplicationRelationships(any(WorkloadsFilter.class)))
                .thenReturn(Mono.just(Arrays.asList(rel)));
        mockTimeKeeper();

        Mono<ResponseEntity<Workloads>> result = controller.getLegacyWorkloads("", "");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().getApplications().size());
                })
                .verifyComplete();
    }

    @Test
    void getLegacyWorkloads_whenNoLegacyApps_returnsNotFound() {
        when(service.getLegacyApplications(any(WorkloadsFilter.class)))
                .thenReturn(Mono.empty());
        when(service.getLegacyApplicationRelationships(any(WorkloadsFilter.class)))
                .thenReturn(Mono.empty());

        assertNotFound(controller.getLegacyWorkloads("", ""));
    }

    @Test
    void getLegacyWorkloads_withStackFilter() {
        AppDetail app = AppDetail.builder().appName("legacy-app").stack("cflinuxfs2").build();
        AppRelationship rel = AppRelationship.builder().appName("legacy-app").build();

        when(service.getLegacyApplications(any(WorkloadsFilter.class)))
                .thenReturn(Mono.just(Arrays.asList(app)));
        when(service.getLegacyApplicationRelationships(any(WorkloadsFilter.class)))
                .thenReturn(Mono.just(Arrays.asList(rel)));
        mockTimeKeeper();

        assertOk(controller.getLegacyWorkloads("cflinuxfs2", ""));
    }

    @Test
    void getLegacyWorkloads_withServiceOfferingsFilter() {
        AppDetail app = AppDetail.builder().appName("legacy-app").build();
        AppRelationship rel = AppRelationship.builder().appName("legacy-app").build();

        when(service.getLegacyApplications(any(WorkloadsFilter.class)))
                .thenReturn(Mono.just(Arrays.asList(app)));
        when(service.getLegacyApplicationRelationships(any(WorkloadsFilter.class)))
                .thenReturn(Mono.just(Arrays.asList(rel)));
        mockTimeKeeper();

        assertOk(controller.getLegacyWorkloads("", "mysql,redis"));
    }
}
