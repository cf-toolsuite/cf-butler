package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.cftoolsuite.cfapp.domain.Policies;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.task.PoliciesLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PoliciesControllerTest {

    private PoliciesService policiesService;
    private PoliciesLoader policiesLoader;
    private PoliciesController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        policiesService = mock(PoliciesService.class);
        policiesLoader = mock(PoliciesLoader.class);
        controller = new PoliciesController(policiesService, policiesLoader);
    }

    @Test
    void listAllPolicies_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findAll()).thenReturn(Mono.just(policies));

        Mono<ResponseEntity<Policies>> result = controller.listAllPolicies();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(policies, response.getBody());
                })
                .verifyComplete();

        verify(policiesService).findAll();
    }

    @Test
    void listAllPolicies_whenEmpty_returnsNotFound() {
        when(policiesService.findAll()).thenReturn(Mono.empty());

        Mono<ResponseEntity<Policies>> result = controller.listAllPolicies();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(policiesService).findAll();
    }

    @Test
    void obtainApplicationPolicy_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findApplicationPolicyById("policy-1")).thenReturn(Mono.just(policies));

        Mono<ResponseEntity<Policies>> result = controller.obtainApplicationPolicy("policy-1");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(policies, response.getBody());
                })
                .verifyComplete();

        verify(policiesService).findApplicationPolicyById("policy-1");
    }

    @Test
    void obtainApplicationPolicy_whenEmpty_returnsNotFound() {
        when(policiesService.findApplicationPolicyById("policy-1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Policies>> result = controller.obtainApplicationPolicy("policy-1");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(policiesService).findApplicationPolicyById("policy-1");
    }

    @Test
    void obtainEndpointPolicy_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findEndpointPolicyById("ep-1")).thenReturn(Mono.just(policies));

        Mono<ResponseEntity<Policies>> result = controller.obtainEndpointPolicy("ep-1");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(policies, response.getBody());
                })
                .verifyComplete();

        verify(policiesService).findEndpointPolicyById("ep-1");
    }

    @Test
    void obtainEndpointPolicy_whenEmpty_returnsNotFound() {
        when(policiesService.findEndpointPolicyById("ep-1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Policies>> result = controller.obtainEndpointPolicy("ep-1");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(policiesService).findEndpointPolicyById("ep-1");
    }

    @Test
    void obtainHygienePolicy_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findHygienePolicyById("hp-1")).thenReturn(Mono.just(policies));

        Mono<ResponseEntity<Policies>> result = controller.obtainHygienePolicy("hp-1");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(policies, response.getBody());
                })
                .verifyComplete();

        verify(policiesService).findHygienePolicyById("hp-1");
    }

    @Test
    void obtainHygienePolicy_whenEmpty_returnsNotFound() {
        when(policiesService.findHygienePolicyById("hp-1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Policies>> result = controller.obtainHygienePolicy("hp-1");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(policiesService).findHygienePolicyById("hp-1");
    }

    @Test
    void obtainLegacyPolicy_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findLegacyPolicyById("lp-1")).thenReturn(Mono.just(policies));

        Mono<ResponseEntity<Policies>> result = controller.obtainLegacyPolicy("lp-1");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(policies, response.getBody());
                })
                .verifyComplete();

        verify(policiesService).findLegacyPolicyById("lp-1");
    }

    @Test
    void obtainLegacyPolicy_whenEmpty_returnsNotFound() {
        when(policiesService.findLegacyPolicyById("lp-1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Policies>> result = controller.obtainLegacyPolicy("lp-1");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(policiesService).findLegacyPolicyById("lp-1");
    }

    @Test
    void obtainQueryPolicy_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findQueryPolicyById("qp-1")).thenReturn(Mono.just(policies));

        Mono<ResponseEntity<Policies>> result = controller.obtainQueryPolicy("qp-1");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(policies, response.getBody());
                })
                .verifyComplete();

        verify(policiesService).findQueryPolicyById("qp-1");
    }

    @Test
    void obtainQueryPolicy_whenEmpty_returnsNotFound() {
        when(policiesService.findQueryPolicyById("qp-1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Policies>> result = controller.obtainQueryPolicy("qp-1");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(policiesService).findQueryPolicyById("qp-1");
    }

    @Test
    void obtainServiceInstancePolicy_whenDataAvailable_returnsOk() {
        Policies policies = Policies.builder().build();

        when(policiesService.findServiceInstancePolicyById("sip-1")).thenReturn(Mono.just(policies));

        Mono<ResponseEntity<Policies>> result = controller.obtainServiceInstancePolicy("sip-1");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(policies, response.getBody());
                })
                .verifyComplete();

        verify(policiesService).findServiceInstancePolicyById("sip-1");
    }

    @Test
    void obtainServiceInstancePolicy_whenEmpty_returnsNotFound() {
        when(policiesService.findServiceInstancePolicyById("sip-1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<Policies>> result = controller.obtainServiceInstancePolicy("sip-1");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

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

        Mono<ResponseEntity<Void>> result = controller.refreshPolicies();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();
    }
}
