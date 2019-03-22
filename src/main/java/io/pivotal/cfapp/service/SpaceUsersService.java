package io.pivotal.cfapp.service;

import java.util.Map;
import java.util.Set;

import io.pivotal.cfapp.domain.SpaceUsers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SpaceUsersService {

	Mono<Void> deleteAll();

	Mono<SpaceUsers> save(SpaceUsers entity);

	Flux<SpaceUsers> findAll();

	Mono<SpaceUsers> findByOrganizationAndSpace(String organization, String space);

	Mono<Integer> count();

	Mono<Map<String, Integer>> countByOrganization();

	Mono<Set<String>> obtainUniqueUsernames();

}