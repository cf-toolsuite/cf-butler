package io.pivotal.cfapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.repository.JdbcAppDetailRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Profile("jdbc")
@Service
public class JdbcAppDetailService implements AppDetailService {

	private JdbcAppDetailRepository repo;

	@Autowired
	public JdbcAppDetailService(JdbcAppDetailRepository repo) {
		this.repo = repo;
	}

	@Override
	public Mono<Void> deleteAll() {
		return repo.deleteAll();
	}

	@Override
	public Mono<AppDetail> save(AppDetail entity) {
		return repo.save(entity);
	}
	
	@Override
	public Mono<AppDetail> findByAppId(String appId) {
		return repo.findByAppId(appId);
	}

	@Override
	public Flux<AppDetail> findAll() {
		return repo.findAll();
	}

	@Override
	public Flux<Tuple2<AppDetail, ApplicationPolicy>> findByApplicationPolicy(ApplicationPolicy policy, boolean mayHaveServiceBindings) {
		return repo.findByApplicationPolicy(policy, mayHaveServiceBindings);
	}

}
