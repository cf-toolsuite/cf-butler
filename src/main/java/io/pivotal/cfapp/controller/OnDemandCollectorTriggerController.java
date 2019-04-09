package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.task.OrganizationsTask;
import reactor.core.publisher.Mono;

@Profile("test")
@RestController
public class OnDemandCollectorTriggerController {

	private OrganizationsTask collector;

	@Autowired
	public OnDemandCollectorTriggerController(OrganizationsTask collector) {
		this.collector = collector;
	}

	@PostMapping("/collect")
	public Mono<ResponseEntity<Void>> triggerCollection() {
		collector.collect();
		return Mono.just(ResponseEntity.accepted().build());
	}

}
