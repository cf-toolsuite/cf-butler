package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.cftoolsuite.cfapp.domain.SnapshotDetail;
import org.cftoolsuite.cfapp.domain.SnapshotSummary;
import org.cftoolsuite.cfapp.service.SnapshotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import org.springframework.http.HttpStatus;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SnapshotControllerTest extends ControllerTestBase {

    private SnapshotService snapshotService;
    private SnapshotController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        snapshotService = mock(SnapshotService.class);
        controller = new SnapshotController(snapshotService, tkService);
    }

    @Test
    void getApplicationInstanceCsvReport_whenTimeKeeperHasData_returnsOk() {
        String csvReport = "app_id,app_name,org\napp1,app1,org1";

        mockTimeKeeper();
        when(snapshotService.assembleCsvAIReport(COLLECTED)).thenReturn(Mono.just(csvReport));

        assertOkBody(controller.getApplicationInstanceCsvReport(), csvReport);

        verify(snapshotService).assembleCsvAIReport(COLLECTED);
    }

    @Test
    void getApplicationInstanceCsvReport_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.getApplicationInstanceCsvReport());

        verifyNoInteractions(snapshotService);
    }

    @Test
    void getApplicationRelationshipsCsvReport_whenTimeKeeperHasData_returnsOk() {
        String csvReport = "source,destination,type\napp1,app2,route";

        mockTimeKeeper();
        when(snapshotService.assembleCsvRelationshipsReport(COLLECTED)).thenReturn(Mono.just(csvReport));

        assertOkBody(controller.getApplicationRelationshipsCsvReport(), csvReport);

        verify(snapshotService).assembleCsvRelationshipsReport(COLLECTED);
    }

    @Test
    void getApplicationRelationshipsCsvReport_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.getApplicationRelationshipsCsvReport());

        verifyNoInteractions(snapshotService);
    }

    @Test
    void getDetail_whenDataAvailable_returnsOkWithSnapshotDetail() {
        SnapshotDetail detail = SnapshotDetail.builder().build();

        mockTimeKeeper();
        when(snapshotService.assembleSnapshotDetail()).thenReturn(Mono.just(detail));

        Mono<ResponseEntity<SnapshotDetail>> result = controller.getDetail();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(detail, response.getBody());
                    assertFalse(response.getHeaders().isEmpty());
                })
                .verifyComplete();

        verify(snapshotService).assembleSnapshotDetail();
    }

    @Test
    void getDetail_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.getDetail());

        verifyNoInteractions(snapshotService);
    }

    @Test
    void getServiceInstanceCsvReport_whenTimeKeeperHasData_returnsOk() {
        String csvReport = "si_id,name,service\nsi1,si1,mysql";

        mockTimeKeeper();
        when(snapshotService.assembleCsvSIReport(COLLECTED)).thenReturn(Mono.just(csvReport));

        assertOkBody(controller.getServiceInstanceCsvReport(), csvReport);

        verify(snapshotService).assembleCsvSIReport(COLLECTED);
    }

    @Test
    void getServiceInstanceCsvReport_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.getServiceInstanceCsvReport());

        verifyNoInteractions(snapshotService);
    }

    @Test
    void getSummary_whenDataAvailable_returnsOkWithSnapshotSummary() {
        SnapshotSummary summary = SnapshotSummary.builder().build();

        mockTimeKeeper();
        when(snapshotService.assembleSnapshotSummary()).thenReturn(Mono.just(summary));

        Mono<ResponseEntity<SnapshotSummary>> result = controller.getSummary();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(summary, response.getBody());
                    assertFalse(response.getHeaders().isEmpty());
                })
                .verifyComplete();

        verify(snapshotService).assembleSnapshotSummary();
    }

    @Test
    void getSummary_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.getSummary());

        verifyNoInteractions(snapshotService);
    }

    @Test
    void getUserAccountCsvReport_whenTimeKeeperHasData_returnsOk() {
        String csvReport = "name,email\nuser1,user1@example.com";

        mockTimeKeeper();
        when(snapshotService.assembleCsvUserAccountReport(COLLECTED)).thenReturn(Mono.just(csvReport));

        assertOkBody(controller.getUserAccountCsvReport(), csvReport);

        verify(snapshotService).assembleCsvUserAccountReport(COLLECTED);
    }

    @Test
    void getUserAccountCsvReport_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.getUserAccountCsvReport());

        verifyNoInteractions(snapshotService);
    }
}
