package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import org.cftoolsuite.cfapp.domain.SnapshotDetail;
import org.cftoolsuite.cfapp.domain.SnapshotSummary;
import org.cftoolsuite.cfapp.service.SnapshotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    static Stream<Arguments> csvReportTypes() {
        return Stream.of(
                arguments("ApplicationInstance", "app_id,app_name,org\napp1,app1,org1"),
                arguments("ApplicationRelationships", "source,destination,type\napp1,app2,route"),
                arguments("ServiceInstance", "si_id,name,service\nsi1,si1,mysql"),
                arguments("UserAccount", "name,email\nuser1,user1@example.com")
        );
    }

    @ParameterizedTest(name = "{0} CSV report - timekeeper has data")
    @MethodSource("csvReportTypes")
    void getCsvReport_whenTimeKeeperHasData_returnsOk(String type, String csvReport) {
        mockTimeKeeper();
        stubService(type, Mono.just(csvReport));
        assertOkBody(invokeController(type), csvReport);
    }

    @ParameterizedTest(name = "{0} CSV report - timekeeper empty")
    @MethodSource("csvReportTypes")
    void getCsvReport_whenTimeKeeperEmpty_returnsNotFound(String type, String csvReport) {
        mockTimeKeeperEmpty();
        assertNotFound(invokeController(type));
    }

    private void stubService(String type, Mono<String> mono) {
        switch (type) {
            case "ApplicationInstance" -> when(snapshotService.assembleCsvAIReport(COLLECTED)).thenReturn(mono);
            case "ApplicationRelationships" -> when(snapshotService.assembleCsvRelationshipsReport(COLLECTED)).thenReturn(mono);
            case "ServiceInstance" -> when(snapshotService.assembleCsvSIReport(COLLECTED)).thenReturn(mono);
            case "UserAccount" -> when(snapshotService.assembleCsvUserAccountReport(COLLECTED)).thenReturn(mono);
        }
    }

    private Mono<? extends ResponseEntity<?>> invokeController(String type) {
        return switch (type) {
            case "ApplicationInstance" -> controller.getApplicationInstanceCsvReport();
            case "ApplicationRelationships" -> controller.getApplicationRelationshipsCsvReport();
            case "ServiceInstance" -> controller.getServiceInstanceCsvReport();
            case "UserAccount" -> controller.getUserAccountCsvReport();
            default -> throw new IllegalArgumentException(type);
        };
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
    }

    @Test
    void getDetail_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.getDetail());
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
    }

    @Test
    void getSummary_whenTimeKeeperEmpty_returnsNotFound() {
        mockTimeKeeperEmpty();

        assertNotFound(controller.getSummary());
    }
}
