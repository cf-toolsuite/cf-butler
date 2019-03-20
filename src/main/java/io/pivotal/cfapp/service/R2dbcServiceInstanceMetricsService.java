package io.pivotal.cfapp.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.repository.R2dbcServiceInstanceMetricsRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

@Service
public class R2dbcServiceInstanceMetricsService implements ServiceInstanceMetricsService {

	private final R2dbcServiceInstanceMetricsRepository repo;

	@Autowired
	public R2dbcServiceInstanceMetricsService(R2dbcServiceInstanceMetricsRepository repo) {
		this.repo = repo;
	}

	@Override
	public Flux<Tuple2<String, Long>> byOrganization() {
		return repo.byOrganization();
	}

	@Override
	public Flux<Tuple2<String, Long>> byService() {
		return repo.byService();
	}

	@Override
	public Flux<Tuple3<String, String, Long>> byServiceAndPlan() {
		return repo.byServiceAndPlan();
	}

	@Override
	public Mono<Long> totalServiceInstances() {
		return repo.totalServiceInstances();
	}

	@Override
	public Flux<Tuple2<String, Long>> totalVelocity() {
		return repo.totalVelocity();
	}

}