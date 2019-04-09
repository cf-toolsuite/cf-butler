package io.pivotal.cfapp.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.repository.R2dbcSpaceUsersRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class R2dbcSpaceUsersService implements SpaceUsersService {

	private R2dbcSpaceUsersRepository repo;

	@Autowired
	public R2dbcSpaceUsersService(R2dbcSpaceUsersRepository repo) {
		this.repo = repo;
	}

	@Override
	public Mono<Void> deleteAll() {
		return repo.deleteAll();
	}

	@Override
	public Mono<SpaceUsers> save(SpaceUsers entity) {
		return repo.save(entity);
	}

	@Override
	public Flux<SpaceUsers> findAll() {
		return repo.findAll();
	}

	@Override
	public Mono<SpaceUsers> findByOrganizationAndSpace(String organization, String space) {
		return repo.findByOrganizationAndSpace(organization, space);
	}

	@Override
	public Mono<Integer> count() {
		// a single user may belong to multiple orgs/spaces
		// iterate spaces collecting unique usernames, ignore assigned roles
		final Set<String> usernames = new HashSet<>();
		return repo
				.findAll()
					.map(su -> usernames.addAll(su.getUsers()))
					.then(Mono.just(usernames))
					.map(u -> usernames.size());
	}

	public Mono<Map<String, Integer>> countByOrganization() {
		// a single user may belong to multiple orgs/spaces
		// iterate spaces collecting unique usernames, ignore assigned roles
		final Map<String, Set<String>> usernames = new HashMap<>();
		return repo
				.findAll()
					.map(su -> addOrgUsers(usernames, su.getOrganization(), su.getUsers()))
					.then(Mono.just(usernames))
					.map(m -> tallyOrgUsers(m));

	}

	private Map<String, Set<String>> addOrgUsers(Map<String, Set<String>> map, String org, Set<String> usernames) {
		if (map.get(org) != null) {
			Set<String> u = map.get(org);
			u.addAll(usernames);
			map.put(org, u);
		} else {
			map.put(org, usernames);
		}
		return map;
	}

	private Map<String, Integer> tallyOrgUsers(Map<String, Set<String>> map) {
		final Map<String, Integer> result = new HashMap<>();
		map.forEach((k, v) -> result.put(k, v.size()));
		return result;
	}

	@Override
	public Mono<Set<String>> obtainUniqueUsernames() {
		// a single user may belong to multiple orgs/spaces
		// iterate spaces collecting unique usernames
		final Set<String> usernames = new HashSet<>();
		return repo
				.findAll()
					.map(su -> usernames.addAll(su.getUsers()))
					.then(Mono.justOrEmpty(usernames));
	}

}
