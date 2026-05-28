package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.cftoolsuite.cfapp.domain.accounting.application.AppUsageReport;
import org.cftoolsuite.cfapp.domain.accounting.service.ServiceUsageReport;
import org.cftoolsuite.cfapp.domain.accounting.task.TaskUsageReport;
import org.cftoolsuite.cfapp.service.UsageCache;
import org.cftoolsuite.cfapp.service.UsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

class UsageControllerTest extends ControllerTestBase {

    private UsageCache cache;
    private UsageService service;
    private UsageController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        cache = mock(UsageCache.class);
        service = mock(UsageService.class);
        controller = new UsageController(cache, service);
    }

    @Test
    void getOrganizationApplicationUsageReport_whenDataAvailable_returnsOk() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);
        String json = "{\"report\":\"data\"}";

        when(service.getApplicationUsage("myorg", start, end)).thenReturn(Mono.just(json));

        assertOkBody(controller.getOrganizationApplicationUsageReport("myorg", start, end), json);

        verify(service).getApplicationUsage("myorg", start, end);
    }

    @Test
    void getOrganizationApplicationUsageReport_whenEmpty_returnsNotFound() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        when(service.getApplicationUsage("myorg", start, end)).thenReturn(Mono.empty());

        assertNotFound(controller.getOrganizationApplicationUsageReport("myorg", start, end));

        verify(service).getApplicationUsage("myorg", start, end);
    }

    @Test
    void getOrganizationServiceUsageReport_whenDataAvailable_returnsOk() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);
        String json = "{\"report\":\"data\"}";

        when(service.getServiceUsage("myorg", start, end)).thenReturn(Mono.just(json));

        assertOkBody(controller.getOrganizationServiceUsageReport("myorg", start, end), json);

        verify(service).getServiceUsage("myorg", start, end);
    }

    @Test
    void getOrganizationServiceUsageReport_whenEmpty_returnsNotFound() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        when(service.getServiceUsage("myorg", start, end)).thenReturn(Mono.empty());

        assertNotFound(controller.getOrganizationServiceUsageReport("myorg", start, end));

        verify(service).getServiceUsage("myorg", start, end);
    }

    @Test
    void getOrganizationTaskUsageReport_whenDataAvailable_returnsOk() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);
        String json = "{\"report\":\"data\"}";

        when(service.getTaskUsage("myorg", start, end)).thenReturn(Mono.just(json));

        assertOkBody(controller.getOrganizationTaskUsageReport("myorg", start, end), json);

        verify(service).getTaskUsage("myorg", start, end);
    }

    @Test
    void getOrganizationTaskUsageReport_whenEmpty_returnsNotFound() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        when(service.getTaskUsage("myorg", start, end)).thenReturn(Mono.empty());

        assertNotFound(controller.getOrganizationTaskUsageReport("myorg", start, end));

        verify(service).getTaskUsage("myorg", start, end);
    }

    @Test
    void getSystemWideApplicationUsageReport_whenDataAvailable_returnsOk() {
        AppUsageReport report = AppUsageReport.builder().reportTime("2024-01-01").build();

        when(cache.getApplicationReport()).thenReturn(report);

        assertOkBody(controller.getSystemWideApplicationUsageReport(), report);

        verify(cache).getApplicationReport();
    }

    @Test
    void getSystemWideApplicationUsageReport_whenEmpty_returnsNotFound() {
        when(cache.getApplicationReport()).thenReturn(null);

        assertNotFound(controller.getSystemWideApplicationUsageReport());

        verify(cache).getApplicationReport();
    }

    @Test
    void getSystemWideServiceUsageReport_whenDataAvailable_returnsOk() {
        ServiceUsageReport report = ServiceUsageReport.builder().reportTime("2024-01-01").build();

        when(cache.getServiceReport()).thenReturn(report);

        assertOkBody(controller.getSystemWideServiceUsageReport(), report);

        verify(cache).getServiceReport();
    }

    @Test
    void getSystemWideServiceUsageReport_whenEmpty_returnsNotFound() {
        when(cache.getServiceReport()).thenReturn(null);

        assertNotFound(controller.getSystemWideServiceUsageReport());

        verify(cache).getServiceReport();
    }

    @Test
    void getSystemWideTaskUsageReport_whenDataAvailable_returnsOk() {
        TaskUsageReport report = TaskUsageReport.builder().reportTime("2024-01-01").build();

        when(cache.getTaskReport()).thenReturn(report);

        assertOkBody(controller.getSystemWideTaskUsageReport(), report);

        verify(cache).getTaskReport();
    }

    @Test
    void getSystemWideTaskUsageReport_whenEmpty_returnsNotFound() {
        when(cache.getTaskReport()).thenReturn(null);

        assertNotFound(controller.getSystemWideTaskUsageReport());

        verify(cache).getTaskReport();
    }
}
