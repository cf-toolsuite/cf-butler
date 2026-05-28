package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;

import org.cftoolsuite.cfapp.domain.SnapshotDetail;
import org.cftoolsuite.cfapp.domain.SnapshotSummary;
import org.cftoolsuite.cfapp.service.SnapshotService;
import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SnapshotControllerTest {

    private SnapshotService snapshotService;
    private TimeKeeperService tkService;
    private SnapshotController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        snapshotService = mock(SnapshotService.class);
        tkService = mock(TimeKeeperService.class);
        controller = new SnapshotController(snapshotService, tkService);
    }

    @Test
    void getApplicationInstanceCsvReport_whenTimeKeeperHasData_returnsOk() {
        LocalDateTime collectedTime = LocalDateTime.of(2024, 1, 15, 10, 30);
        String csvReport = "app_id,app_name,org\napp1,app1,org1";

        when(tkService.findOne()).thenReturn(Mono.just(collectedTime));
        when(snapshotService.assembleCsvAIReport(collectedTime)).thenReturn(Mono.just(csvReport));

        Mono<ResponseEntity<String>> result = controller.getApplicationInstanceCsvReport();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(csvReport, response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(snapshotService).assembleCsvAIReport(collectedTime);
    }

    @Test
    void getApplicationInstanceCsvReport_whenTimeKeeperEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<String>> result = controller.getApplicationInstanceCsvReport();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(snapshotService);
    }

    @Test
    void getApplicationRelationshipsCsvReport_whenTimeKeeperHasData_returnsOk() {
        LocalDateTime collectedTime = LocalDateTime.of(2024, 1, 15, 10, 30);
        String csvReport = "source,destination,type\napp1,app2,route";

        when(tkService.findOne()).thenReturn(Mono.just(collectedTime));
        when(snapshotService.assembleCsvRelationshipsReport(collectedTime)).thenReturn(Mono.just(csvReport));

        Mono<ResponseEntity<String>> result = controller.getApplicationRelationshipsCsvReport();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(csvReport, response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(snapshotService).assembleCsvRelationshipsReport(collectedTime);
    }

    @Test
    void getApplicationRelationshipsCsvReport_whenTimeKeeperEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<String>> result = controller.getApplicationRelationshipsCsvReport();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(snapshotService);
    }

    @Test
    void getDetail_whenDataAvailable_returnsOkWithSnapshotDetail() {
        SnapshotDetail detail = SnapshotDetail.builder().build();

        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(snapshotService.assembleSnapshotDetail()).thenReturn(Mono.just(detail));

        Mono<ResponseEntity<SnapshotDetail>> result = controller.getDetail();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(detail, response.getBody());
                    assertFalse(response.getHeaders().isEmpty());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(snapshotService).assembleSnapshotDetail();
    }

    @Test
    void getDetail_whenTimeKeeperEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<SnapshotDetail>> result = controller.getDetail();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(snapshotService);
    }

    @Test
    void getServiceInstanceCsvReport_whenTimeKeeperHasData_returnsOk() {
        LocalDateTime collectedTime = LocalDateTime.of(2024, 1, 15, 10, 30);
        String csvReport = "si_id,name,service\nsi1,si1,mysql";

        when(tkService.findOne()).thenReturn(Mono.just(collectedTime));
        when(snapshotService.assembleCsvSIReport(collectedTime)).thenReturn(Mono.just(csvReport));

        Mono<ResponseEntity<String>> result = controller.getServiceInstanceCsvReport();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(csvReport, response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(snapshotService).assembleCsvSIReport(collectedTime);
    }

    @Test
    void getServiceInstanceCsvReport_whenTimeKeeperEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<String>> result = controller.getServiceInstanceCsvReport();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(snapshotService);
    }

    @Test
    void getSummary_whenDataAvailable_returnsOkWithSnapshotSummary() {
        SnapshotSummary summary = SnapshotSummary.builder().build();

        when(tkService.findOne()).thenReturn(Mono.just(LocalDateTime.of(2024, 1, 15, 10, 30)));
        when(snapshotService.assembleSnapshotSummary()).thenReturn(Mono.just(summary));

        Mono<ResponseEntity<SnapshotSummary>> result = controller.getSummary();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(summary, response.getBody());
                    assertFalse(response.getHeaders().isEmpty());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(snapshotService).assembleSnapshotSummary();
    }

    @Test
    void getSummary_whenTimeKeeperEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<SnapshotSummary>> result = controller.getSummary();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(snapshotService);
    }

    @Test
    void getUserAccountCsvReport_whenTimeKeeperHasData_returnsOk() {
        LocalDateTime collectedTime = LocalDateTime.of(2024, 1, 15, 10, 30);
        String csvReport = "name,email\nuser1,user1@example.com";

        when(tkService.findOne()).thenReturn(Mono.just(collectedTime));
        when(snapshotService.assembleCsvUserAccountReport(collectedTime)).thenReturn(Mono.just(csvReport));

        Mono<ResponseEntity<String>> result = controller.getUserAccountCsvReport();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(csvReport, response.getBody());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verify(snapshotService).assembleCsvUserAccountReport(collectedTime);
    }

    @Test
    void getUserAccountCsvReport_whenTimeKeeperEmpty_returnsNotFound() {
        when(tkService.findOne()).thenReturn(Mono.empty());

        Mono<ResponseEntity<String>> result = controller.getUserAccountCsvReport();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(tkService).findOne();
        verifyNoInteractions(snapshotService);
    }
}
