package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.task.LatestProductReleasesTask;
import io.pivotal.cfapp.task.OrganizationsTask;
import io.pivotal.cfapp.task.ProductsTask;
import reactor.core.publisher.Mono;

@Profile("on-demand")
@RestController
public class OnDemandCollectorTriggerController {

	private OrganizationsTask orgCollector;
	private ProductsTask productsCollector;
	private LatestProductReleasesTask productReleasesCollector;

	@Autowired
	public OnDemandCollectorTriggerController(
		OrganizationsTask orgCollector,
		ProductsTask productsCollector,
		LatestProductReleasesTask productReleasesCollector) {
		this.orgCollector = orgCollector;
		this.productsCollector = productsCollector;
		this.productReleasesCollector = productReleasesCollector;
	}

	@PostMapping("/collect")
	public Mono<ResponseEntity<Void>> triggerCollection() {
		orgCollector.collect();
		productsCollector.collect();
		productReleasesCollector.collect();
		return Mono.just(ResponseEntity.accepted().build());
	}

}
