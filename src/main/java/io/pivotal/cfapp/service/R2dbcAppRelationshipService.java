package io.pivotal.cfapp.service;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.AppRelationship;
import io.pivotal.cfapp.repository.R2dbcAppRelationshipRepository;
import io.r2dbc.spi.R2dbcException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class R2dbcAppRelationshipService implements AppRelationshipService {

	private R2dbcAppRelationshipRepository repo;

	@Autowired
	public R2dbcAppRelationshipService(R2dbcAppRelationshipRepository repo) {
		this.repo = repo;
	}

	@Override
	public Mono<Void> deleteAll() {
		return repo.deleteAll();
	}

	@Override
	public Mono<AppRelationship> save(AppRelationship entity) {
		return repo
				.save(entity)
				.onErrorContinue(R2dbcException.class,
					(ex, data) -> log.error("Problem saving application releationship {}.", entity, ex))
				.onErrorContinue(SQLException.class,
					(ex, data) -> log.error("Problem saving application releationship {}.", entity, ex));

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
