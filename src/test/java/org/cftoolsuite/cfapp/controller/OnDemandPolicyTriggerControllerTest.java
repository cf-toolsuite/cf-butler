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

import reactor.core.publisher.Mono;

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

        assertAccepted(controller.triggerPolicyExecution());
    }

    @Test
    void triggerPolicyExecution_whenTaskMapEmpty_returnsAccepted() {
        when(service.getTaskMap()).thenReturn(Mono.just(new HashMap<>()));

        assertAccepted(controller.triggerPolicyExecution());
    }
}
