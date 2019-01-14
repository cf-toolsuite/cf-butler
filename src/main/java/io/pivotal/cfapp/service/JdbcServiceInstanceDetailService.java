package io.pivotal.cfapp.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import io.pivotal.cfapp.repository.JdbcServiceInstanceDetailRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Service
public class JdbcServiceInstanceDetailService implements ServiceInstanceDetailService {

	private JdbcServiceInstanceDetailRepository repo;

	public JdbcServiceInstanceDetailService(
			JdbcServiceInstanceDetailRepository repo) {
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
	public Flux<ServiceInstanceDetail> findByServiceInstancePolicy(ServiceInstancePolicy policy) {
		return repo.findByServiceInstancePolicy(policy);
	}

}
