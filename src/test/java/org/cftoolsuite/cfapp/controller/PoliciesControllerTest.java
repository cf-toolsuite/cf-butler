package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import org.cftoolsuite.cfapp.domain.Policies;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.task.PoliciesLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

class PoliciesControllerTest extends ControllerTestBase {

    private PoliciesService policiesService;
    private PoliciesLoader policiesLoader;
    private PoliciesController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        policiesService = mock(PoliciesService.class);
        policiesLoader = mock(PoliciesLoader.class);
        controller = new PoliciesController(policiesService, policiesLoader);
    }

    static Stream<Arguments> policyTypes() {
        return Stream.of(
                arguments("Application", "policy-1"),
                arguments("Endpoint", "ep-1"),
                arguments("Hygiene", "hp-1"),
                arguments("Legacy", "lp-1"),
                arguments("Query", "qp-1"),
                arguments("ServiceInstance", "sip-1")
        );
    }

    @ParameterizedTest(name = "{0} - data available")
    @MethodSource("policyTypes")
    void obtainPolicy_whenDataAvailable_returnsOk(String type, String id) {
        Policies policies = Policies.builder().build();

        stubService(type, id, Mono.just(policies));
        assertOkBody(invokeController(type, id), policies);
    }

    @ParameterizedTest(name = "{0} - empty")
    @MethodSource("policyTypes")
    void obtainPolicy_whenEmpty_returnsNotFound(String type, String id) {
        stubService(type, id, Mono.empty());
        assertNotFound(invokeController(type, id));
    }

    private void stubService(String type, String id, Mono<Policies> mono) {
        switch (type) {
            case "Application" -> when(policiesService.findApplicationPolicyById(id)).thenReturn(mono);
            case "Endpoint" -> when(policiesService.findEndpointPolicyById(id)).thenReturn(mono);
            case "Hygiene" -> when(policiesService.findHygienePolicyById(id)).thenReturn(mono);
            case "Legacy" -> when(policiesService.findLegacyPolicyById(id)).thenReturn(mono);
            case "Query" -> when(policiesService.findQueryPolicyById(id)).thenReturn(mono);
            case "ServiceInstance" -> when(policiesService.findServiceInstancePolicyById(id)).thenReturn(mono);
            default -> throw new IllegalArgumentException(type);
        }
    }

    private Mono<? extends ResponseEntity<?>> invokeController(String type, String id) {
        return switch (type) {
            case "Application" -> controller.obtainApplicationPolicy(id);
            case "Endpoint" -> controller.obtainEndpointPolicy(id);
            case "Hygiene" -> controller.obtainHygienePolicy(id);
            case "Legacy" -> controller.obtainLegacyPolicy(id);
            case "Query" -> controller.obtainQueryPolicy(id);
            case "ServiceInstance" -> controller.obtainServiceInstancePolicy(id);
            default -> throw new IllegalArgumentException(type);
        };
    }

    @Test
    void listAllPolicies_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findAll()).thenReturn(Mono.just(policies));

        assertOkBody(controller.listAllPolicies(), policies);
    }

    @Test
    void listAllPolicies_whenEmpty_returnsNotFound() {
        when(policiesService.findAll()).thenReturn(Mono.empty());

        assertNotFound(controller.listAllPolicies());
    }

    @Test
    void refreshPolicies_whenLoaderAvailable_returnsAccepted() {
        assertAccepted(controller.refreshPolicies());
    }

    @Test
    void refreshPolicies_whenLoaderNull_returnsNotFound() {
        controller = new PoliciesController(policiesService, null);

        assertNotFound(controller.refreshPolicies());
    }
}
