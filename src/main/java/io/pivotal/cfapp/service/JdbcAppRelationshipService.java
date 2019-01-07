package io.pivotal.cfapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.AppRelationship;
import io.pivotal.cfapp.repository.JdbcAppRelationshipRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Service
public class JdbcAppRelationshipService implements AppRelationshipService {

	private JdbcAppRelationshipRepository repo;

	@Autowired
	public JdbcAppRelationshipService(JdbcAppRelationshipRepository repo) {
		this.repo = repo;
	}

	@Override
	public Mono<Void> deleteAll() {
		return repo.deleteAll();
	}

	@Override
	public Mono<AppRelationship> save(AppRelationship entity) {
		return repo.save(entity);
	}

	@Override
	public Flux<AppRelationship> findAll() {
		return repo.findAll();
	}

	@Override
	public Flux<AppRelationship> findByApplicationId(String applicationId) {
		return repo.findByApplicationId(applicationId);
	}

}
