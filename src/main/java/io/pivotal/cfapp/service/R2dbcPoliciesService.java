package io.pivotal.cfapp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pivotal.cfapp.config.GitSettings;
import io.pivotal.cfapp.domain.ApplicationOperation;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.domain.ServiceInstanceOperation;
import io.pivotal.cfapp.repository.R2dbcPoliciesRepository;
import reactor.core.publisher.Mono;

@Service
public class R2dbcPoliciesService implements PoliciesService {

    private static final String UNSUPPORTED_OP_MESSAGE = "Policies are managed in a git repository.";

    private final R2dbcPoliciesRepository repo;
    private final GitSettings settings;

    public R2dbcPoliciesService(
            R2dbcPoliciesRepository repo,
            GitSettings settings) {
        this.repo = repo;
        this.settings = settings;
    }

    @Override
    @Transactional
    public Mono<Void> deleteAll() {
        return repo.deleteAll();
    }

    @Override
    @Transactional
    public Mono<Void> deleteApplicationPolicyById(String id) {
        if (settings.isVersionManaged()) {
            throw new UnsupportedOperationException(UNSUPPORTED_OP_MESSAGE);
        }
        return repo.deleteApplicationPolicyById(id);
    }

    @Override
    @Transactional
    public Mono<Void> deleteEndpointPolicyById(String id) {
        if (settings.isVersionManaged()) {
            throw new UnsupportedOperationException(UNSUPPORTED_OP_MESSAGE);
        }
        return repo.deleteEndpointPolicyById(id);
    }

    @Override
    @Transactional
    public Mono<Void> deleteHygienePolicyById(String id) {
        if (settings.isVersionManaged()) {
            throw new UnsupportedOperationException(UNSUPPORTED_OP_MESSAGE);
        }
        return repo.deleteHygienePolicyById(id);
    }

    @Override
    @Transactional
    public Mono<Void> deleteResourceNotificationPolicyById(String id) {
        if (settings.isVersionManaged()) {
            throw new UnsupportedOperationException(UNSUPPORTED_OP_MESSAGE);
        }
        return repo.deleteResourceNotificationPolicyById(id);
    }

    @Override
    @Transactional
    public Mono<Void> deleteLegacyPolicyById(String id) {
        if (settings.isVersionManaged()) {
            throw new UnsupportedOperationException(UNSUPPORTED_OP_MESSAGE);
        }
        return repo.deleteLegacyPolicyById(id);
    }

    @Override
    @Transactional
    public Mono<Void> deleteQueryPolicyById(String id) {
        if (settings.isVersionManaged()) {
            throw new UnsupportedOperationException(UNSUPPORTED_OP_MESSAGE);
        }
        return repo.deleteQueryPolicyById(id);
    }

    @Override
    @Transactional
    public Mono<Void> deleteServiceInstancePolicyById(String id) {
        if (settings.isVersionManaged()) {
            throw new UnsupportedOperationException(UNSUPPORTED_OP_MESSAGE);
        }
        return repo.deleteServiceInstancePolicyById(id);
    }

    @Override
    public Mono<Policies> findAll() {
        return repo.findAll();
    }

    @Override
    public Mono<Policies> findAllEndpointPolicies() {
        return repo.findAllEndpointPolicies();
    }

    @Override
    public Mono<Policies> findAllHygienePolicies() {
        return repo.findAllHygienePolicies();
    }

    @Override
    public Mono<Policies> findAllResourceNotificationPolicies() {
        return repo.findAllResourceNotificationPolicies();
    }

    @Override
    public Mono<Policies> findAllLegacyPolicies() {
        return repo.findAllLegacyPolicies();
    }

    @Override
    public Mono<Policies> findAllQueryPolicies() {
        return repo.findAllQueryPolicies();
    }

    @Override
    public Mono<Policies> findApplicationPolicyById(String id) {
        return repo.findApplicationPolicyById(id);
    }

    @Override
    public Mono<Policies> findByApplicationOperation(ApplicationOperation operation) {
        return repo.findByApplicationOperation(operation);
    }

    @Override
    public Mono<Policies> findByServiceInstanceOperation(ServiceInstanceOperation operation) {
        return repo.findByServiceInstanceOperation(operation);
    }

    @Override
    public Mono<Policies> findEndpointPolicyById(String id) {
        return repo.findEndpointPolicyById(id);
    }

    @Override
    public Mono<Policies> findHygienePolicyById(String id) {
        return repo.findHygienePolicyById(id);
    }

    @Override
    public Mono<Policies> findResourceNotificationPolicyById(String id) {
        return repo.findResourceNotificationPolicyById(id);
    }

    @Override
    public Mono<Policies> findLegacyPolicyById(String id) {
        return repo.findLegacyPolicyById(id);
    }

    @Override
    public Mono<Policies> findQueryPolicyById(String id) {
        return repo.findQueryPolicyById(id);
    }

    @Override
    public Mono<Policies> findServiceInstancePolicyById(String id) {
        return repo.findServiceInstancePolicyById(id);
    }

    @Override
    @Transactional
    public Mono<Policies> save(Policies entity) {
        return repo.save(entity);
    }

}
