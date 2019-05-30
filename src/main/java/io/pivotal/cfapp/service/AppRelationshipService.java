package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.AppRelationship;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AppRelationshipService {

	Mono<Void> deleteAll();

	Mono<AppRelationship> save(AppRelationship entity);

	Flux<AppRelationship> findAll();

	Flux<AppRelationship> findByApplicationId(String applicationId);

}