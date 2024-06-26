package org.cftoolsuite.cfapp.service;

import java.util.HashMap;
import java.util.Map;

import org.cftoolsuite.cfapp.config.GitSettings;
import org.cftoolsuite.cfapp.domain.ApplicationOperation;
import org.cftoolsuite.cfapp.domain.ApplicationPolicy;
import org.cftoolsuite.cfapp.domain.EndpointPolicy;
import org.cftoolsuite.cfapp.domain.HygienePolicy;
import org.cftoolsuite.cfapp.domain.LegacyPolicy;
import org.cftoolsuite.cfapp.domain.Policies;
import org.cftoolsuite.cfapp.domain.QueryPolicy;
import org.cftoolsuite.cfapp.domain.ResourceNotificationPolicy;
import org.cftoolsuite.cfapp.domain.ServiceInstanceOperation;
import org.cftoolsuite.cfapp.domain.ServiceInstancePolicy;
import org.cftoolsuite.cfapp.repository.R2dbcPoliciesRepository;
import org.cftoolsuite.cfapp.task.EndpointPolicyExecutorTask;
import org.cftoolsuite.cfapp.task.HygienePolicyExecutorTask;
import org.cftoolsuite.cfapp.task.LegacyWorkloadReportingTask;
import org.cftoolsuite.cfapp.task.PolicyExecutorTask;
import org.cftoolsuite.cfapp.task.QueryPolicyExecutorTask;
import org.cftoolsuite.cfapp.task.ResourceNotificationPolicyExecutorTask;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
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

    @Override
    public Mono<Map<String, Class<? extends PolicyExecutorTask>>> getTaskMap() {
        Mono<Policies> policiesMono = repo.findAll();
        Flux<Map<String, Class<? extends PolicyExecutorTask>>> mapsFlux = Flux.merge(
            policiesMono
                .flatMapMany(p -> Flux.fromIterable(p.getApplicationPolicies()))
                .collectMap(ApplicationPolicy::getId, ap -> ApplicationOperation.getTaskType(ap.getOperation())),
            policiesMono
                .flatMapMany(p -> Flux.fromIterable(p.getEndpointPolicies()))
                .collectMap(EndpointPolicy::getId, ep -> EndpointPolicyExecutorTask.class),
            policiesMono
                .flatMapMany(p -> Flux.fromIterable(p.getHygienePolicies()))
                .collectMap(HygienePolicy::getId, hp -> HygienePolicyExecutorTask.class),
            policiesMono
                .flatMapMany(p -> Flux.fromIterable(p.getLegacyPolicies()))
                .collectMap(LegacyPolicy::getId, lp -> LegacyWorkloadReportingTask.class),
            policiesMono
                .flatMapMany(p -> Flux.fromIterable(p.getQueryPolicies()))
                .collectMap(QueryPolicy::getId, qp -> QueryPolicyExecutorTask.class),
            policiesMono
                .flatMapMany(p -> Flux.fromIterable(p.getResourceNotificationPolicies()))
                .collectMap(ResourceNotificationPolicy::getId, rnp -> ResourceNotificationPolicyExecutorTask.class),
            policiesMono
                .flatMapMany(p -> Flux.fromIterable(p.getServiceInstancePolicies()))
                .collectMap(ServiceInstancePolicy::getId, sip -> ServiceInstanceOperation.getTaskType(sip.getOperation()))
        );
        return
            mapsFlux
                .reduce(
                    new HashMap<String, Class<? extends PolicyExecutorTask>>(), (acc, map) -> {
                        acc.putAll(map);
                        return acc;
                    }
                );
    }

}
