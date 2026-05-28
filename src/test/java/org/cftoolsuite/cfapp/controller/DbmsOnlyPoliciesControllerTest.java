package org.cftoolsuite.cfapp.controller;

import static org.mockito.Mockito.*;

import org.cftoolsuite.cfapp.domain.Policies;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @Test
    void deleteAllPolicies_whenSuccessful_completes() {
        when(policiesService.deleteAll()).thenReturn(Mono.fromRunnable(() -> {}));

        Mono<ResponseEntity<Void>> result = controller.deleteAllPolicies();
        StepVerifier.create(result).expectNextCount(0).verifyComplete();
        verify(policiesService).deleteAll();
    }

    @Test
    void deleteAllPolicies_whenEmpty_completes() {
        when(policiesService.deleteAll()).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = controller.deleteAllPolicies();
        StepVerifier.create(result).verifyComplete();
        verify(policiesService).deleteAll();
    }

    @Test
    void deleteApplicationPolicy_whenSuccessful_completes() {
        when(policiesService.deleteApplicationPolicyById("policy-1")).thenReturn(Mono.fromRunnable(() -> {}));

        Mono<ResponseEntity<Void>> result = controller.deleteApplicationPolicy("policy-1");
        StepVerifier.create(result).expectNextCount(0).verifyComplete();
        verify(policiesService).deleteApplicationPolicyById("policy-1");
    }

    @Test
    void deleteApplicationPolicy_whenEmpty_completes() {
        when(policiesService.deleteApplicationPolicyById("policy-1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = controller.deleteApplicationPolicy("policy-1");
        StepVerifier.create(result).verifyComplete();
        verify(policiesService).deleteApplicationPolicyById("policy-1");
    }

    @Test
    void deleteEndpointPolicy_whenSuccessful_completes() {
        when(policiesService.deleteEndpointPolicyById("ep-1")).thenReturn(Mono.fromRunnable(() -> {}));

        Mono<ResponseEntity<Void>> result = controller.deleteEndpointPolicy("ep-1");
        StepVerifier.create(result).expectNextCount(0).verifyComplete();
        verify(policiesService).deleteEndpointPolicyById("ep-1");
    }

    @Test
    void deleteEndpointPolicy_whenEmpty_completes() {
        when(policiesService.deleteEndpointPolicyById("ep-1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = controller.deleteEndpointPolicy("ep-1");
        StepVerifier.create(result).verifyComplete();
        verify(policiesService).deleteEndpointPolicyById("ep-1");
    }

    @Test
    void deleteHygienePolicy_whenSuccessful_completes() {
        when(policiesService.deleteHygienePolicyById("hp-1")).thenReturn(Mono.fromRunnable(() -> {}));

        Mono<ResponseEntity<Void>> result = controller.deleteHygienePolicy("hp-1");
        StepVerifier.create(result).expectNextCount(0).verifyComplete();
        verify(policiesService).deleteHygienePolicyById("hp-1");
    }

    @Test
    void deleteHygienePolicy_whenEmpty_completes() {
        when(policiesService.deleteHygienePolicyById("hp-1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = controller.deleteHygienePolicy("hp-1");
        StepVerifier.create(result).verifyComplete();
        verify(policiesService).deleteHygienePolicyById("hp-1");
    }

    @Test
    void deleteResourceNotificationPolicy_whenSuccessful_completes() {
        when(policiesService.deleteResourceNotificationPolicyById("rnp-1")).thenReturn(Mono.fromRunnable(() -> {}));

        Mono<ResponseEntity<Void>> result = controller.deleteResourceNotificationPolicy("rnp-1");
        StepVerifier.create(result).expectNextCount(0).verifyComplete();
        verify(policiesService).deleteResourceNotificationPolicyById("rnp-1");
    }

    @Test
    void deleteResourceNotificationPolicy_whenEmpty_completes() {
        when(policiesService.deleteResourceNotificationPolicyById("rnp-1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = controller.deleteResourceNotificationPolicy("rnp-1");
        StepVerifier.create(result).verifyComplete();
        verify(policiesService).deleteResourceNotificationPolicyById("rnp-1");
    }

    @Test
    void deleteLegacyPolicy_whenSuccessful_completes() {
        when(policiesService.deleteLegacyPolicyById("lp-1")).thenReturn(Mono.fromRunnable(() -> {}));

        Mono<ResponseEntity<Void>> result = controller.deleteLegacyPolicy("lp-1");
        StepVerifier.create(result).expectNextCount(0).verifyComplete();
        verify(policiesService).deleteLegacyPolicyById("lp-1");
    }

    @Test
    void deleteLegacyPolicy_whenEmpty_completes() {
        when(policiesService.deleteLegacyPolicyById("lp-1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = controller.deleteLegacyPolicy("lp-1");
        StepVerifier.create(result).verifyComplete();
        verify(policiesService).deleteLegacyPolicyById("lp-1");
    }

    @Test
    void deleteQueryPolicy_whenSuccessful_completes() {
        when(policiesService.deleteQueryPolicyById("qp-1")).thenReturn(Mono.fromRunnable(() -> {}));

        Mono<ResponseEntity<Void>> result = controller.deleteQueryPolicy("qp-1");
        StepVerifier.create(result).expectNextCount(0).verifyComplete();
        verify(policiesService).deleteQueryPolicyById("qp-1");
    }

    @Test
    void deleteQueryPolicy_whenEmpty_completes() {
        when(policiesService.deleteQueryPolicyById("qp-1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = controller.deleteQueryPolicy("qp-1");
        StepVerifier.create(result).verifyComplete();
        verify(policiesService).deleteQueryPolicyById("qp-1");
    }

    @Test
    void deleteServiceInstancePolicy_whenSuccessful_completes() {
        when(policiesService.deleteServiceInstancePolicyById("sip-1")).thenReturn(Mono.fromRunnable(() -> {}));

        Mono<ResponseEntity<Void>> result = controller.deleteServiceInstancePolicy("sip-1");
        StepVerifier.create(result).expectNextCount(0).verifyComplete();
        verify(policiesService).deleteServiceInstancePolicyById("sip-1");
    }

    @Test
    void deleteServiceInstancePolicy_whenEmpty_completes() {
        when(policiesService.deleteServiceInstancePolicyById("sip-1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = controller.deleteServiceInstancePolicy("sip-1");
        StepVerifier.create(result).verifyComplete();
        verify(policiesService).deleteServiceInstancePolicyById("sip-1");
    }

    @Test
    void establishPolicies_whenSuccessful_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.save(policies)).thenReturn(Mono.just(policies));

        assertOkBody(controller.establishPolicies(policies), policies);

        verify(policiesService).save(policies);
    }
}
