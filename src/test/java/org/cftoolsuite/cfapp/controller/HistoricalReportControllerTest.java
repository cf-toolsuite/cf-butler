package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.domain.HistoricalRecord;
import org.cftoolsuite.cfapp.service.HistoricalRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class HistoricalReportControllerTest extends ControllerTestBase {

    private HistoricalRecordService historicalRecordService;
    private HistoricalReportController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        historicalRecordService = mock(HistoricalRecordService.class);
        PasSettings settings = mock(PasSettings.class);
        when(settings.getApiHost()).thenReturn("api.example.com");
        controller = new HistoricalReportController(settings, historicalRecordService);
    }

    @Test
    void generateReport_whenNoDateRange_returnsOk() {
        HistoricalRecord record = HistoricalRecord.builder().build();

        when(historicalRecordService.findAll()).thenReturn(Flux.just(record));

        Mono<ResponseEntity<String>> result = controller.generateReport(null, null);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void generateReport_whenNoDateRange_empty_returnsOk() {
        when(historicalRecordService.findAll()).thenReturn(Flux.empty());

        assertOk(controller.generateReport(null, null));
    }

    @Test
    void generateReport_whenValidDateRange_returnsOk() {
        HistoricalRecord record = HistoricalRecord.builder().build();

        when(historicalRecordService.findByDateRange(TEST_START, TEST_END)).thenReturn(Flux.just(record));

        Mono<ResponseEntity<String>> result = controller.generateReport(TEST_START, TEST_END);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void generateReport_whenValidDateRange_empty_returnsOk() {
        when(historicalRecordService.findByDateRange(TEST_START, TEST_END)).thenReturn(Flux.empty());

        assertOk(controller.generateReport(TEST_START, TEST_END));
    }

    @Test
    void generateReport_whenInvalidDateRange_returnsBadRequest() {
        LocalDate start = LocalDate.of(2024, 2, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        assertBadRequest(controller.generateReport(start, end));
    }

    @Test
    void generateReport_whenOnlyStartProvided_returnsOk() {
        HistoricalRecord record = HistoricalRecord.builder().build();

        when(historicalRecordService.findAll()).thenReturn(Flux.just(record));

        Mono<ResponseEntity<String>> result = controller.generateReport(TEST_START, null);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();
    }
}
