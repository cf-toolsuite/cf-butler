package io.pivotal.cfapp.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.config.ButlerSettings.PoliciesSettings;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.repository.JdbcPoliciesRepository;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Service
public class JdbcPoliciesService implements PoliciesService {

	private static final String UNSUPPORTED_OP_MESSAGE = "Policies are managed in a git repository.";
	
	private final JdbcPoliciesRepository repo;
	private final PoliciesSettings settings;
	
	public JdbcPoliciesService(
			JdbcPoliciesRepository repo,
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
		if (settings.isVersionManaged()) {
			throw new UnsupportedOperationException(UNSUPPORTED_OP_MESSAGE);
		}
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

}
