package io.pivotal.cfapp.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import io.pivotal.cfapp.repository.R2dbcServiceInstanceDetailRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
public class R2dbcServiceInstanceDetailService implements ServiceInstanceDetailService {

	private R2dbcServiceInstanceDetailRepository repo;

	public R2dbcServiceInstanceDetailService(R2dbcServiceInstanceDetailRepository repo) {
		this.repo = repo;
	}

	@Override
	public Mono<ServiceInstanceDetail> save(ServiceInstanceDetail entity) {
		return repo.save(entity);
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
