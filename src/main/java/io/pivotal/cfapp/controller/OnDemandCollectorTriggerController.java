package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.task.AppInfoTask;
import io.pivotal.cfapp.task.ServiceInstanceInfoTask;
import reactor.core.publisher.Mono;

@Profile("test")
@RestController
public class OnDemandCollectorTriggerController {

	private AppInfoTask appInfoCollector;
	private ServiceInstanceInfoTask serviceInstanceInfoCollector;
	
	@Autowired
	public OnDemandCollectorTriggerController(
			AppInfoTask appInfoCollector,
			ServiceInstanceInfoTask serviceInstanceInfoCollector
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
