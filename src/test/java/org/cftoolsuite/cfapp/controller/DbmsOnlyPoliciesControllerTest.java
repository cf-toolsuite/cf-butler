package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import org.cftoolsuite.cfapp.domain.Policies;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DbmsOnlyPoliciesControllerTest extends ControllerTestBase {

    private PoliciesService policiesService;
    private DbmsOnlyPoliciesController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        policiesService = mock(PoliciesService.class);
        controller = new DbmsOnlyPoliciesController(policiesService);
    }

    static Stream<Arguments> deleteTypes() {
        return Stream.of(
                arguments("Application", "policy-1"),
                arguments("Endpoint", "ep-1"),
                arguments("Hygiene", "hp-1"),
                arguments("ResourceNotification", "rnp-1"),
                arguments("Legacy", "lp-1"),
                arguments("Query", "qp-1"),
                arguments("ServiceInstance", "sip-1")
        );
    }

    @ParameterizedTest(name = "delete {0} - successful")
    @MethodSource("deleteTypes")
    void deletePolicy_whenSuccessful_completes(String type, String id) {
        stubDelete(type, id, Mono.fromRunnable(() -> {}));
        StepVerifier.create(invokeDelete(type, id)).expectNextCount(0).verifyComplete();
    }

    @ParameterizedTest(name = "delete {0} - empty")
    @MethodSource("deleteTypes")
    void deletePolicy_whenEmpty_completes(String type, String id) {
        stubDelete(type, id, Mono.empty());
        StepVerifier.create(invokeDelete(type, id)).verifyComplete();
    }

    private void stubDelete(String type, String id, Mono<Void> mono) {
        switch (type) {
            case "Application" -> when(policiesService.deleteApplicationPolicyById(id)).thenReturn(mono);
            case "Endpoint" -> when(policiesService.deleteEndpointPolicyById(id)).thenReturn(mono);
            case "Hygiene" -> when(policiesService.deleteHygienePolicyById(id)).thenReturn(mono);
            case "ResourceNotification" -> when(policiesService.deleteResourceNotificationPolicyById(id)).thenReturn(mono);
            case "Legacy" -> when(policiesService.deleteLegacyPolicyById(id)).thenReturn(mono);
            case "Query" -> when(policiesService.deleteQueryPolicyById(id)).thenReturn(mono);
            case "ServiceInstance" -> when(policiesService.deleteServiceInstancePolicyById(id)).thenReturn(mono);
            default -> throw new IllegalArgumentException(type);
        }
    }

    private Mono<ResponseEntity<Void>> invokeDelete(String type, String id) {
        return switch (type) {
            case "Application" -> controller.deleteApplicationPolicy(id);
            case "Endpoint" -> controller.deleteEndpointPolicy(id);
            case "Hygiene" -> controller.deleteHygienePolicy(id);
            case "ResourceNotification" -> controller.deleteResourceNotificationPolicy(id);
            case "Legacy" -> controller.deleteLegacyPolicy(id);
            case "Query" -> controller.deleteQueryPolicy(id);
            case "ServiceInstance" -> controller.deleteServiceInstancePolicy(id);
            default -> throw new IllegalArgumentException(type);
        };
    }

    @Test
    void deleteAllPolicies_whenSuccessful_completes() {
        when(policiesService.deleteAll()).thenReturn(Mono.fromRunnable(() -> {}));

        Mono<ResponseEntity<Void>> result = controller.deleteAllPolicies();
        StepVerifier.create(result).expectNextCount(0).verifyComplete();
    }

    @Test
    void deleteAllPolicies_whenEmpty_completes() {
        when(policiesService.deleteAll()).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = controller.deleteAllPolicies();
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void establishPolicies_whenSuccessful_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.save(policies)).thenReturn(Mono.just(policies));

        assertOkBody(controller.establishPolicies(policies), policies);
    }
}
