package org.cftoolsuite.cfapp.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.task.PolicyExecutorTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OnDemandPolicyTriggerControllerTest extends ControllerTestBase {

    private BeanFactory factory;
    private PoliciesService service;
    private OnDemandPolicyTriggerController controller;

    @BeforeEach
    void setUp() {
        initMocks();
        factory = mock(BeanFactory.class);
        service = mock(PoliciesService.class);
        controller = new OnDemandPolicyTriggerController(factory, service);
    }

    @Test
    void triggerPolicyExecution_whenTaskMapAvailable_returnsAccepted() {
        PolicyExecutorTask task = mock(PolicyExecutorTask.class);

        Map<String, Class<? extends PolicyExecutorTask>> taskMap = new HashMap<>();
        taskMap.put("policy-1", (Class<? extends PolicyExecutorTask>) task.getClass());

        when(service.getTaskMap()).thenReturn(Mono.just(taskMap));
        when(factory.getBean(any(Class.class))).thenReturn(task);

        Mono<ResponseEntity<Void>> result = controller.triggerPolicyExecution();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
                })
                .verifyComplete();

        verify(service).getTaskMap();
        verify(factory).getBean(any(Class.class));
        verify(task).execute("policy-1");
    }

    @Test
    void triggerPolicyExecution_whenTaskMapEmpty_returnsAccepted() {
        when(service.getTaskMap()).thenReturn(Mono.just(new HashMap<>()));

        Mono<ResponseEntity<Void>> result = controller.triggerPolicyExecution();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
                })
                .verifyComplete();

        verify(service).getTaskMap();
    }
}
