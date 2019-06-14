package io.pivotal.cfapp.service;

import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import io.pivotal.cfapp.repository.R2dbcServiceInstanceDetailRepository;
import io.r2dbc.spi.R2dbcException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Slf4j
@Service
public class R2dbcServiceInstanceDetailService implements ServiceInstanceDetailService {

	private R2dbcServiceInstanceDetailRepository repo;

	public R2dbcServiceInstanceDetailService(R2dbcServiceInstanceDetailRepository repo) {
		this.repo = repo;
	}

	@Override
	public Mono<ServiceInstanceDetail> save(ServiceInstanceDetail entity) {
		return repo
				.save(entity)
				.onErrorContinue(R2dbcException.class,
					(ex, data) -> log.error("Problem saving service instance {}.", entity, ex))
				.onErrorContinue(SQLException.class,
					(ex, data) -> log.error("Problem saving service instance {}.", entity, ex));
	}

	@Override
	public Flux<ServiceInstanceDetail> findAll() {
		return repo.findAll();
	}

	@Override
	public Mono<Void> deleteAll() {
		return repo.deleteAll();
	}

	@Override
	public Flux<Tuple2<ServiceInstanceDetail, ServiceInstancePolicy>> findByServiceInstancePolicy(
			ServiceInstancePolicy policy) {
		return repo.findByServiceInstancePolicy(policy);
	}

	@Override
	public Flux<ServiceInstanceDetail> findByDateRange(LocalDate start, LocalDate end) {
		return repo.findByDateRange(start, end);
	}

}
