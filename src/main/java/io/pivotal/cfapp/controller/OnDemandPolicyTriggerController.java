package io.pivotal.cfapp.controller;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.task.PolicyExecutorTask;
import reactor.core.publisher.Mono;

@Profile("on-demand")
@RestController
public class OnDemandPolicyTriggerController {

    private ListableBeanFactory factory;

    @Autowired
    public OnDemandPolicyTriggerController(ListableBeanFactory factory) {
        this.factory = factory;
    }

    @PostMapping("/policies/execute")
    public Mono<ResponseEntity<Void>> triggerPolicyExection() {
        Map<String, PolicyExecutorTask> taskMap = factory.getBeansOfType(PolicyExecutorTask.class);
        Collection<PolicyExecutorTask> tasks = taskMap.values();
        tasks.forEach(PolicyExecutorTask::execute);
        return Mono.just(ResponseEntity.accepted().build());
    }

}
