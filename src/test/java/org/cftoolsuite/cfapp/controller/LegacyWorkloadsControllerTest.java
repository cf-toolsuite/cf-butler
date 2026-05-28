package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.domain.AppRelationship;
import org.cftoolsuite.cfapp.domain.Workloads;
import org.cftoolsuite.cfapp.domain.WorkloadsFilter;
import org.cftoolsuite.cfapp.service.LegacyWorkloadsService;
import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class LegacyWorkloadsControllerTest {

    private LegacyWorkloadsService service;
    private TimeKeeperService tkService;
    private LegacyWorkloadsController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = mock(LegacyWorkloadsService.class);
        tkService = mock(TimeKeeperService.class);
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
        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));

        Mono<ResponseEntity<Workloads>> result = controller.getLegacyWorkloads("", "");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(1, response.getBody().getApplications().size());
                })
                .verifyComplete();

        verify(service).getLegacyApplications(any(WorkloadsFilter.class));
        verify(service).getLegacyApplicationRelationships(any(WorkloadsFilter.class));
        verify(tkService).findOne();
    }

    @Test
    void getLegacyWorkloads_whenNoLegacyApps_returnsNotFound() {
        when(service.getLegacyApplications(any(WorkloadsFilter.class)))
                .thenReturn(Mono.empty());
        when(service.getLegacyApplicationRelationships(any(WorkloadsFilter.class)))
                .thenReturn(Mono.empty());

        Mono<ResponseEntity<Workloads>> result = controller.getLegacyWorkloads("", "");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(service).getLegacyApplications(any(WorkloadsFilter.class));
        verify(service).getLegacyApplicationRelationships(any(WorkloadsFilter.class));
    }

    @Test
    void getLegacyWorkloads_withStackFilter() {
        AppDetail app = AppDetail.builder().appName("legacy-app").stack("cflinuxfs2").build();
        AppRelationship rel = AppRelationship.builder().appName("legacy-app").build();

        when(service.getLegacyApplications(any(WorkloadsFilter.class)))
                .thenReturn(Mono.just(Arrays.asList(app)));
        when(service.getLegacyApplicationRelationships(any(WorkloadsFilter.class)))
                .thenReturn(Mono.just(Arrays.asList(rel)));
        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));

        Mono<ResponseEntity<Workloads>> result = controller.getLegacyWorkloads("cflinuxfs2", "");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void getLegacyWorkloads_withServiceOfferingsFilter() {
        AppDetail app = AppDetail.builder().appName("legacy-app").build();
        AppRelationship rel = AppRelationship.builder().appName("legacy-app").build();

        when(service.getLegacyApplications(any(WorkloadsFilter.class)))
                .thenReturn(Mono.just(Arrays.asList(app)));
        when(service.getLegacyApplicationRelationships(any(WorkloadsFilter.class)))
                .thenReturn(Mono.just(Arrays.asList(rel)));
        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));

        Mono<ResponseEntity<Workloads>> result = controller.getLegacyWorkloads("", "mysql,redis");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                })
                .verifyComplete();
    }
}
