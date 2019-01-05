package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.Policies;
import reactor.core.publisher.Mono;

public interface PoliciesService {

	Mono<Policies> save(Policies entity);
	Mono<Policies> findAll();
	Mono<Void> deleteAll();
}
