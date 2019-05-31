package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.task.OrganizationsTask;
import io.pivotal.cfapp.task.ProductsAndReleasesTask;
import reactor.core.publisher.Mono;

@Profile("on-demand")
@RestController
public class OnDemandCollectorTriggerController {

	private OrganizationsTask orgCollector;
	private ProductsAndReleasesTask productsAndReleasesCollector;

	@Autowired
	public OnDemandCollectorTriggerController(
		OrganizationsTask orgCollector,
		ProductsAndReleasesTask productsAndReleasesCollector) {
		this.orgCollector = orgCollector;
		this.productsAndReleasesCollector = productsAndReleasesCollector;
	}

	@PostMapping("/collect")
	public Mono<ResponseEntity<Void>> triggerCollection() {
		orgCollector.collect();
		productsAndReleasesCollector.collect();
		return Mono.just(ResponseEntity.accepted().build());
	}

}
