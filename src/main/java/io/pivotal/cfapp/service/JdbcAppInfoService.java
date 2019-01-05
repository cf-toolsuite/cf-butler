package io.pivotal.cfapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.repository.JdbcAppInfoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Service
public class JdbcAppInfoService implements AppInfoService {

	private JdbcAppInfoRepository repo;

	@Autowired
	public JdbcAppInfoService(JdbcAppInfoRepository repo) {
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
	public Flux<AppDetail> findAll() {
		return repo.findAll();
	}

}
