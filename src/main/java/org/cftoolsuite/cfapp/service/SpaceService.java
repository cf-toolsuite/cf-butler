package org.cftoolsuite.cfapp.service;

import org.cftoolsuite.cfapp.domain.Space;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SpaceService {

    Mono<Void> deleteAll();

    Flux<Space> findAll();

    Mono<Space> save(Space entity);

}
