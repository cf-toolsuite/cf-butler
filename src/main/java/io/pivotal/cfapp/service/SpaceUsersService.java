package io.pivotal.cfapp.service;

import java.util.Map;

import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.domain.UserAccounts;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SpaceUsersService {

	Mono<Void> deleteAll();

	Mono<SpaceUsers> save(SpaceUsers entity);

	Flux<SpaceUsers> findAll();

	Mono<SpaceUsers> findByOrganizationAndSpace(String organization, String space);

	Mono<Long> totalUserAccounts();

	Mono<Long> totalServiceAccounts();

	Mono<Map<String, Integer>> countByOrganization();

	Flux<UserAccounts> obtainUserAccounts();

	Flux<String> obtainUserAccountNames();

	Flux<String> obtainServiceAccountNames();

	Flux<String> obtainAccountNames();

}