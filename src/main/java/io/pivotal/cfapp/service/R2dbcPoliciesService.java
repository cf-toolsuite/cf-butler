package io.pivotal.cfapp.service;

import org.springframework.stereotype.Service;

import io.pivotal.cfapp.config.PoliciesSettings;
import io.pivotal.cfapp.domain.ApplicationOperation;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.domain.ServiceInstanceOperation;
import io.pivotal.cfapp.repository.R2dbcPoliciesRepository;
import reactor.core.publisher.Mono;

@Service
public class R2dbcPoliciesService implements PoliciesService {

	private static final String UNSUPPORTED_OP_MESSAGE = "Policies are managed in a git repository.";

	private final R2dbcPoliciesRepository repo;
	private final PoliciesSettings settings;

	public R2dbcPoliciesService(
			R2dbcPoliciesRepository repo,
			PoliciesSettings settings) {
		this.repo = repo;
		this.settings = settings;
	}

	@Override
	public Mono<Policies> save(Policies entity) {
		return repo.save(entity);
	}

	@Override
	public Mono<Policies> findAll() {
		return repo.findAll();
	}

	@Override
	public Mono<Void> deleteAll() {
		return repo.deleteAll();
	}

	@Override
	public Mono<Policies> findApplicationPolicyById(String id) {
		return repo.findApplicationPolicyById(id);
	}

	@Override
	public Mono<Policies> findServiceInstancePolicyById(String id) {
		return repo.findServiceInstancePolicyById(id);
	}

	@Override
	public Mono<Void> deleteApplicationPolicyById(String id) {
		if (settings.isVersionManaged()) {
			throw new UnsupportedOperationException(UNSUPPORTED_OP_MESSAGE);
		}
		return repo.deleteApplicationPolicyById(id);
	}

	@Override
	public Mono<Void> deleteServiceInstancePolicyById(String id) {
		if (settings.isVersionManaged()) {
			throw new UnsupportedOperationException(UNSUPPORTED_OP_MESSAGE);
		}
		return repo.deleteServicePolicyById(id);
	}

	@Override
	public Mono<Policies> findByApplicationOperation(ApplicationOperation operation) {
		return repo.findByApplicationOperation(operation);
	}

	@Override
	public Mono<Policies> findByServiceInstanceOperation(ServiceInstanceOperation operation) {
		return repo.findByServiceInstanceOperation(operation);
	}

}
