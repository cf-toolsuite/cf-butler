package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.service.PoliciesService;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "cf.policies.provider", havingValue = "dbms")
public class DbmsOnlyPoliciesController {

	private final PoliciesService policiesService;

	@Autowired
	public DbmsOnlyPoliciesController(
			PoliciesService policiesService) {
		this.policiesService = policiesService;
	}

	@PostMapping("/policies")
	public Mono<ResponseEntity<Policies>> establishPolicies(@RequestBody Policies policies) {
		return policiesService.save(policies)
								.map(p -> ResponseEntity.ok(p));
	}

	@DeleteMapping("/policies/application/{id}")
	public Mono<ResponseEntity<Void>> deleteApplicationPolicy(@PathVariable String id) {
		return policiesService.deleteApplicationPolicyById(id)
								.map(p -> ResponseEntity.ok(p));
	}

	@DeleteMapping("/policies/serviceInstance/{id}")
	public Mono<ResponseEntity<Void>> deleteServiceInstancePolicy(@PathVariable String id) {
		return policiesService.deleteServiceInstancePolicyById(id)
								.map(p -> ResponseEntity.ok(p));
	}

	@DeleteMapping("/policies")
	public Mono<ResponseEntity<Void>> deleteAllPolicies() {
		return policiesService.deleteAll()
								.map(p -> ResponseEntity.ok(p));
	}
}
