package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.cftoolsuite.cfapp.domain.Policies;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.task.PoliciesLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

    @Test
    void listAllPolicies_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findAll()).thenReturn(Mono.just(policies));

        assertOkBody(controller.listAllPolicies(), policies);

        verify(policiesService).findAll();
    }

    @Test
    void listAllPolicies_whenEmpty_returnsNotFound() {
        when(policiesService.findAll()).thenReturn(Mono.empty());

        assertNotFound(controller.listAllPolicies());

        verify(policiesService).findAll();
    }

    @Test
    void obtainApplicationPolicy_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findApplicationPolicyById("policy-1")).thenReturn(Mono.just(policies));

        assertOkBody(controller.obtainApplicationPolicy("policy-1"), policies);

        verify(policiesService).findApplicationPolicyById("policy-1");
    }

    @Test
    void obtainApplicationPolicy_whenEmpty_returnsNotFound() {
        when(policiesService.findApplicationPolicyById("policy-1")).thenReturn(Mono.empty());

        assertNotFound(controller.obtainApplicationPolicy("policy-1"));

        verify(policiesService).findApplicationPolicyById("policy-1");
    }

    @Test
    void obtainEndpointPolicy_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findEndpointPolicyById("ep-1")).thenReturn(Mono.just(policies));

        assertOkBody(controller.obtainEndpointPolicy("ep-1"), policies);

        verify(policiesService).findEndpointPolicyById("ep-1");
    }

    @Test
    void obtainEndpointPolicy_whenEmpty_returnsNotFound() {
        when(policiesService.findEndpointPolicyById("ep-1")).thenReturn(Mono.empty());

        assertNotFound(controller.obtainEndpointPolicy("ep-1"));

        verify(policiesService).findEndpointPolicyById("ep-1");
    }

    @Test
    void obtainHygienePolicy_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findHygienePolicyById("hp-1")).thenReturn(Mono.just(policies));

        assertOkBody(controller.obtainHygienePolicy("hp-1"), policies);

        verify(policiesService).findHygienePolicyById("hp-1");
    }

    @Test
    void obtainHygienePolicy_whenEmpty_returnsNotFound() {
        when(policiesService.findHygienePolicyById("hp-1")).thenReturn(Mono.empty());

        assertNotFound(controller.obtainHygienePolicy("hp-1"));

        verify(policiesService).findHygienePolicyById("hp-1");
    }

    @Test
    void obtainLegacyPolicy_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findLegacyPolicyById("lp-1")).thenReturn(Mono.just(policies));

        assertOkBody(controller.obtainLegacyPolicy("lp-1"), policies);

        verify(policiesService).findLegacyPolicyById("lp-1");
    }

    @Test
    void obtainLegacyPolicy_whenEmpty_returnsNotFound() {
        when(policiesService.findLegacyPolicyById("lp-1")).thenReturn(Mono.empty());

        assertNotFound(controller.obtainLegacyPolicy("lp-1"));

        verify(policiesService).findLegacyPolicyById("lp-1");
    }

    @Test
    void obtainQueryPolicy_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findQueryPolicyById("qp-1")).thenReturn(Mono.just(policies));

        assertOkBody(controller.obtainQueryPolicy("qp-1"), policies);

        verify(policiesService).findQueryPolicyById("qp-1");
    }

    @Test
    void obtainQueryPolicy_whenEmpty_returnsNotFound() {
        when(policiesService.findQueryPolicyById("qp-1")).thenReturn(Mono.empty());

        assertNotFound(controller.obtainQueryPolicy("qp-1"));

        verify(policiesService).findQueryPolicyById("qp-1");
    }

    @Test
    void obtainServiceInstancePolicy_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findServiceInstancePolicyById("sip-1")).thenReturn(Mono.just(policies));

        assertOkBody(controller.obtainServiceInstancePolicy("sip-1"), policies);

        verify(policiesService).findServiceInstancePolicyById("sip-1");
    }

    @Test
    void obtainServiceInstancePolicy_whenEmpty_returnsNotFound() {
        when(policiesService.findServiceInstancePolicyById("sip-1")).thenReturn(Mono.empty());

        assertNotFound(controller.obtainServiceInstancePolicy("sip-1"));

        verify(policiesService).findServiceInstancePolicyById("sip-1");
    }

    @Test
    void refreshPolicies_whenLoaderAvailable_returnsAccepted() {
        Mono<ResponseEntity<Void>> result = controller.refreshPolicies();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
                })
                .verifyComplete();

        verify(policiesLoader).load();
    }

    @Test
    void refreshPolicies_whenLoaderNull_returnsNotFound() {
        controller = new PoliciesController(policiesService, null);

        assertNotFound(controller.refreshPolicies());
    }
}
