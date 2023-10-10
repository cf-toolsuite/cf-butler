package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.Organization;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationService {

    Mono<Void> deleteAll();

    Flux<Organization> findAll();

    Mono<Organization> save(Organization entity);

}
