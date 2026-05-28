package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.domain.HistoricalRecord;
import org.cftoolsuite.cfapp.service.HistoricalRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class HistoricalReportControllerTest {

    private HistoricalRecordService historicalRecordService;
    private HistoricalReportController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        verify(historicalRecordService).findAll();
    }

    @Test
    void generateReport_whenNoDateRange_empty_returnsOk() {
        when(historicalRecordService.findAll()).thenReturn(Flux.empty());

        Mono<ResponseEntity<String>> result = controller.generateReport(null, null);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                })
                .verifyComplete();

        verify(historicalRecordService).findAll();
    }

    @Test
    void generateReport_whenValidDateRange_returnsOk() {
        HistoricalRecord record = HistoricalRecord.builder().build();
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        when(historicalRecordService.findByDateRange(start, end)).thenReturn(Flux.just(record));

        Mono<ResponseEntity<String>> result = controller.generateReport(start, end);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();

        verify(historicalRecordService).findByDateRange(start, end);
    }

    @Test
    void generateReport_whenValidDateRange_empty_returnsOk() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        when(historicalRecordService.findByDateRange(start, end)).thenReturn(Flux.empty());

        Mono<ResponseEntity<String>> result = controller.generateReport(start, end);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                })
                .verifyComplete();

        verify(historicalRecordService).findByDateRange(start, end);
    }

    @Test
    void generateReport_whenInvalidDateRange_returnsBadRequest() {
        LocalDate start = LocalDate.of(2024, 2, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        Mono<ResponseEntity<String>> result = controller.generateReport(start, end);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                })
                .verifyComplete();

        verifyNoInteractions(historicalRecordService);
    }

    @Test
    void generateReport_whenOnlyStartProvided_returnsOk() {
        HistoricalRecord record = HistoricalRecord.builder().build();
        LocalDate start = LocalDate.of(2024, 1, 1);

        when(historicalRecordService.findAll()).thenReturn(Flux.just(record));

        Mono<ResponseEntity<String>> result = controller.generateReport(start, null);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                })
                .verifyComplete();

        verify(historicalRecordService).findAll();
    }
}
