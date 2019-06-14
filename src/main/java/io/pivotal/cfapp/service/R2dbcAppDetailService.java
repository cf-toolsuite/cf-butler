package io.pivotal.cfapp.service;

import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.repository.R2dbcAppDetailRepository;
import io.r2dbc.spi.R2dbcException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Slf4j
@Service
public class R2dbcAppDetailService implements AppDetailService {

	private R2dbcAppDetailRepository repo;

	@Autowired
	public R2dbcAppDetailService(R2dbcAppDetailRepository repo) {
		this.repo = repo;
	}

	@Override
	public Mono<Void> deleteAll() {
		return repo.deleteAll();
	}

	@Override
	public Mono<AppDetail> save(AppDetail entity) {
		return repo
				.save(entity)
				.onErrorContinue(R2dbcException.class,
					(ex, data) -> log.error("Problem saving application {}.", entity, ex))
				.onErrorContinue(SQLException.class,
					(ex, data) -> log.error("Problem saving application {}.", entity, ex));
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

	@Override
	public Flux<AppDetail> findByDateRange(LocalDate start, LocalDate end) {
		return repo.findByDateRange(start, end);
	}

}
