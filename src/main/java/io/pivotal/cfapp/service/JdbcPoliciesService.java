package io.pivotal.cfapp.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.repository.JdbcPoliciesRepository;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Service
public class JdbcPoliciesService implements PoliciesService {

	private JdbcPoliciesRepository repo;
	
	public JdbcPoliciesService(JdbcPoliciesRepository repo) {
		this.repo = repo;
	}
	
	@Override
	public Mono<Policies> save(Policies entity) {
		return repo.save(entity);
	}

	@Override
	public Mono<Policies> findAll() {
		return repo.findAll();
	}

	@Override
	public Mono<Void> deleteAll() {
		return repo.deleteAll();
	}

}
