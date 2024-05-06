package org.cftoolsuite.cfapp.controller;

import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.task.PolicyExecutorTask;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("on-demand")
@RestController
public class OnDemandPolicyTriggerController {

    private BeanFactory factory;
    private PoliciesService service;

    @Autowired
    public OnDemandPolicyTriggerController(BeanFactory factory, PoliciesService service) {
        this.factory = factory;
        this.service = service;
    }

    @PostMapping("/policies/execute")
    public Mono<ResponseEntity<Void>> triggerPolicyExecution() {
        return service.getTaskMap()
            .flatMapMany(taskTypeMap ->
                Flux.fromIterable(taskTypeMap.entrySet())
                    .flatMap(entry -> {
                        String policyId = entry.getKey();
                        Class<? extends PolicyExecutorTask> taskClass = entry.getValue();
                        PolicyExecutorTask task = factory.getBean(taskClass);
                        return Mono.fromRunnable(() -> task.execute(policyId));
                    }))
            .then(Mono.just(ResponseEntity.accepted().build()));
    }

}
