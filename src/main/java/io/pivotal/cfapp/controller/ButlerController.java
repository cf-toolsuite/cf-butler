package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.service.PoliciesService;
import reactor.core.publisher.Mono;

@RestController
public class ButlerController {

	private final PoliciesService policiesService;
	
	@Autowired
	public ButlerController(
			PoliciesService policiesService) {
		this.policiesService = policiesService;
	}

	@GetMapping(value = { "/report" }, produces = MediaType.TEXT_PLAIN_VALUE)
	public Mono<ResponseEntity<String>> generateReport() {
		// TODO generate historical report of what applications and service instances were removed
		return null;
	}
	
	@PostMapping("/policies")
	public Mono<ResponseEntity<Policies>> establishPolicies(@RequestBody Policies policies) {
		return policiesService.save(policies)
								.map(p -> ResponseEntity.ok(p));
	}
	
	@GetMapping(value = { "/policies" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Policies>> listPolicies() {
		return policiesService.findAll()
								.map(p -> ResponseEntity.ok(p))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/policies")
	public Mono<ResponseEntity<Void>> deletePolicies() {
		return policiesService.deleteAll()
								.map(p -> ResponseEntity.ok(p));
	}
}
