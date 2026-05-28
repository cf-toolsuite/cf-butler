package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import org.cftoolsuite.cfapp.domain.accounting.application.AppUsageReport;
import org.cftoolsuite.cfapp.domain.accounting.service.ServiceUsageReport;
import org.cftoolsuite.cfapp.domain.accounting.task.TaskUsageReport;
import org.cftoolsuite.cfapp.service.UsageCache;
import org.cftoolsuite.cfapp.service.UsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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

    static Stream<Arguments> orgReportTypes() {
        return Stream.of(
                arguments("Application", "myorg", "{\"report\":\"data\"}"),
                arguments("Service", "myorg", "{\"report\":\"data\"}"),
                arguments("Task", "myorg", "{\"report\":\"data\"}")
        );
    }

    @ParameterizedTest(name = "org {0} usage - data available")
    @MethodSource("orgReportTypes")
    void getOrganizationUsageReport_whenDataAvailable_returnsOk(String type, String org, String json) {
        stubServiceOrg(type, org, TEST_START, TEST_END, Mono.just(json));
        assertOkBody(invokeControllerOrg(type, org, TEST_START, TEST_END), json);
    }

    @ParameterizedTest(name = "org {0} usage - empty")
    @MethodSource("orgReportTypes")
    void getOrganizationUsageReport_whenEmpty_returnsNotFound(String type, String org, String json) {
        stubServiceOrg(type, org, TEST_START, TEST_END, Mono.empty());
        assertNotFound(invokeControllerOrg(type, org, TEST_START, TEST_END));
    }

    private void stubServiceOrg(String type, String org, java.time.LocalDate start, java.time.LocalDate end, Mono<String> mono) {
        switch (type) {
            case "Application" -> when(service.getApplicationUsage(org, start, end)).thenReturn(mono);
            case "Service" -> when(service.getServiceUsage(org, start, end)).thenReturn(mono);
            case "Task" -> when(service.getTaskUsage(org, start, end)).thenReturn(mono);
        }
    }

    private Mono<? extends ResponseEntity<?>> invokeControllerOrg(String type, String org, java.time.LocalDate start, java.time.LocalDate end) {
        return switch (type) {
            case "Application" -> controller.getOrganizationApplicationUsageReport(org, start, end);
            case "Service" -> controller.getOrganizationServiceUsageReport(org, start, end);
            case "Task" -> controller.getOrganizationTaskUsageReport(org, start, end);
            default -> throw new IllegalArgumentException(type);
        };
    }

    static Stream<Arguments> systemReportTypes() {
        return Stream.of(
                arguments("Application", AppUsageReport.builder().reportTime("2024-01-01").build()),
                arguments("Service", ServiceUsageReport.builder().reportTime("2024-01-01").build()),
                arguments("Task", TaskUsageReport.builder().reportTime("2024-01-01").build())
        );
    }

    @ParameterizedTest(name = "system-wide {0} report - data available")
    @MethodSource("systemReportTypes")
    void getSystemWideUsageReport_whenDataAvailable_returnsOk(String type, Object report) {
        stubCache(type, report);
        assertOkBody(invokeControllerSystem(type), report);
    }

    @ParameterizedTest(name = "system-wide {0} report - empty")
    @MethodSource("systemReportTypes")
    void getSystemWideUsageReport_whenEmpty_returnsNotFound(String type, Object report) {
        stubCache(type, null);
        assertNotFound(invokeControllerSystem(type));
    }

    private void stubCache(String type, Object value) {
        switch (type) {
            case "Application" -> when(cache.getApplicationReport()).thenReturn((AppUsageReport) value);
            case "Service" -> when(cache.getServiceReport()).thenReturn((ServiceUsageReport) value);
            case "Task" -> when(cache.getTaskReport()).thenReturn((TaskUsageReport) value);
        }
    }

    private Mono<? extends ResponseEntity<?>> invokeControllerSystem(String type) {
        return switch (type) {
            case "Application" -> controller.getSystemWideApplicationUsageReport();
            case "Service" -> controller.getSystemWideServiceUsageReport();
            case "Task" -> controller.getSystemWideTaskUsageReport();
            default -> throw new IllegalArgumentException(type);
        };
    }
}
