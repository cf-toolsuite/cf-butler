package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.service.PoliciesService;
import reactor.core.publisher.Mono;

@RestController
public class PoliciesController {

	private final PoliciesService policiesService;

	@Autowired
	public PoliciesController(
			PoliciesService policiesService) {
		this.policiesService = policiesService;
	}

	@GetMapping(value = { "/policies/application/{id}" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Policies>> obtainApplicationPolicy(@PathVariable String id) {
		return policiesService.findApplicationPolicyById(id)
								.map(p -> ResponseEntity.ok(p))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping(value = { "/policies/serviceInstance/{id}" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Policies>> obtainServiceInstancePolicy(@PathVariable String id) {
		return policiesService.findServiceInstancePolicyById(id)
								.map(p -> ResponseEntity.ok(p))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping(value = { "/policies/query/{id}" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Policies>> obtainQueryPolicy(@PathVariable String id) {
		return policiesService.findQueryPolicyById(id)
								.map(p -> ResponseEntity.ok(p))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping(value = { "/policies" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Policies>> listAllPolicies() {
		return policiesService.findAll()
								.map(p -> ResponseEntity.ok(p))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}

}
