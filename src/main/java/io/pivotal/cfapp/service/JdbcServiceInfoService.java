package io.pivotal.cfapp.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.ServiceDetail;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import io.pivotal.cfapp.repository.JdbcServiceInfoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Service
public class JdbcServiceInfoService implements ServiceInfoService {

	private JdbcServiceInfoRepository repo;

	public JdbcServiceInfoService(
			JdbcServiceInfoRepository repo) {
		this.repo = repo;
	}

	@Override
	public Mono<ServiceDetail> save(ServiceDetail entity) {
		return repo.save(entity);
	}

	@Override
	public Flux<ServiceDetail> findAll() {
		return repo.findAll();
	}

	@Override
	public Mono<Void> deleteAll() {
		return repo.deleteAll();
	}

	@Override
	public Flux<ServiceDetail> findByServiceInstancePolicy(ServiceInstancePolicy policy) {
		return repo.findByServiceInstancePolicy(policy);
	}

}
