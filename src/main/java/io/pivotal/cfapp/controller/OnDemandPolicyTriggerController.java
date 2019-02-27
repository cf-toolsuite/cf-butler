package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.task.AppPolicyExecutorTask;
import io.pivotal.cfapp.task.ServiceInstancePolicyExecutorTask;
import reactor.core.publisher.Mono;

@Profile("test")
@RestController
public class OnDemandPolicyTriggerController {

	private AppPolicyExecutorTask appPolicyExecutor;
	private ServiceInstancePolicyExecutorTask serviceInstancePolicyExecutor;

	@Autowired
	public OnDemandPolicyTriggerController(
			AppPolicyExecutorTask appPolicyExecutor,
			ServiceInstancePolicyExecutorTask serviceInstancePolicyExecutor
			) {
		this.appPolicyExecutor = appPolicyExecutor;
		this.serviceInstancePolicyExecutor = serviceInstancePolicyExecutor;
	}

	@PostMapping("/policies/execute")
	public Mono<ResponseEntity<Void>> triggerPolicyExection() {
		appPolicyExecutor.execute();
		serviceInstancePolicyExecutor.execute();
		return Mono.just(ResponseEntity.accepted().build());
	}

}
