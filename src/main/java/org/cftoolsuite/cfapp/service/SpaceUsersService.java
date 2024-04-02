package org.cftoolsuite.cfapp.service;

import java.util.Map;

import org.cftoolsuite.cfapp.domain.SpaceUsers;
import org.cftoolsuite.cfapp.domain.UserAccounts;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SpaceUsersService {

    Mono<Map<String, Integer>> countByOrganization();

    Mono<Void> deleteAll();

    Flux<SpaceUsers> findAll();

    Flux<SpaceUsers> findByAccountName(String name);

    Mono<SpaceUsers> findByOrganizationAndSpace(String organization, String space);

    Flux<String> obtainAccountNames();

    Flux<String> obtainServiceAccountNames();

    Flux<String> obtainUserAccountNames();

    Flux<UserAccounts> obtainUserAccounts();

    Mono<SpaceUsers> save(SpaceUsers entity);

    Mono<Long> totalServiceAccounts();

    Mono<Long> totalUserAccounts();

}
