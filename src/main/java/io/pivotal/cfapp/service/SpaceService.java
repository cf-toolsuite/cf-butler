package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.Space;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SpaceService {

	Mono<Void> deleteAll();

	Mono<Space> save(Space entity);

	Flux<Space> findAll();

}