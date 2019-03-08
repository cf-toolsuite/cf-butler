package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.task.AppDetailTask;
import io.pivotal.cfapp.task.ServiceInstanceDetailTask;
import reactor.core.publisher.Mono;

@Profile("test")
@RestController
public class OnDemandCollectorTriggerController {

	private AppDetailTask appInfoCollector;
	private ServiceInstanceDetailTask serviceInstanceInfoCollector;

	@Autowired
	public OnDemandCollectorTriggerController(
			AppDetailTask appInfoCollector,
			ServiceInstanceDetailTask serviceInstanceInfoCollector
			) {
		this.appInfoCollector = appInfoCollector;
		this.serviceInstanceInfoCollector = serviceInstanceInfoCollector;
	}

	@PostMapping("/collect")
	public Mono<ResponseEntity<Void>> triggerCollection() {
		appInfoCollector.collect();
		serviceInstanceInfoCollector.collect();
		return Mono.just(ResponseEntity.accepted().build());
	}

}
