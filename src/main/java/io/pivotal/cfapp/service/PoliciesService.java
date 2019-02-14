package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.Policies;
import reactor.core.publisher.Mono;

public interface PoliciesService {
	
	Mono<Policies> save(Policies entity);
	Mono<Policies> findApplicationPolicyById(String id);
	Mono<Policies> findServiceInstancePolicyById(String id);
	Mono<Policies> findAll();
	Mono<Void> deleteApplicationPolicyById(String id);
	Mono<Void> deleteServiceInstancePolicyById(String id);
	Mono<Void> deleteAll();
}
