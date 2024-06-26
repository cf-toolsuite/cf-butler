package org.cftoolsuite.cfapp.service;

import java.util.Map;

import org.cftoolsuite.cfapp.domain.ApplicationOperation;
import org.cftoolsuite.cfapp.domain.Policies;
import org.cftoolsuite.cfapp.domain.ServiceInstanceOperation;
import org.cftoolsuite.cfapp.task.PolicyExecutorTask;

import reactor.core.publisher.Mono;

public interface PoliciesService {

    Mono<Void> deleteAll();
    Mono<Void> deleteApplicationPolicyById(String id);
    Mono<Void> deleteEndpointPolicyById(String id);
    Mono<Void> deleteHygienePolicyById(String id);
    Mono<Void> deleteResourceNotificationPolicyById(String id);
    Mono<Void> deleteLegacyPolicyById(String id);
    Mono<Void> deleteQueryPolicyById(String id);
    Mono<Void> deleteServiceInstancePolicyById(String id);
    Mono<Policies> findAll();
    Mono<Policies> findAllEndpointPolicies();
    Mono<Policies> findAllHygienePolicies();
    Mono<Policies> findAllResourceNotificationPolicies();
    Mono<Policies> findAllLegacyPolicies();
    Mono<Policies> findAllQueryPolicies();
    Mono<Policies> findApplicationPolicyById(String id);
    Mono<Policies> findByApplicationOperation(ApplicationOperation operation);
    Mono<Policies> findByServiceInstanceOperation(ServiceInstanceOperation operation);
    Mono<Policies> findEndpointPolicyById(String id);
    Mono<Policies> findHygienePolicyById(String id);
    Mono<Policies> findResourceNotificationPolicyById(String id);
    Mono<Policies> findLegacyPolicyById(String id);
    Mono<Policies> findQueryPolicyById(String id);
    Mono<Policies> findServiceInstancePolicyById(String id);
    Mono<Policies> save(Policies entity);
    Mono<Map<String, Class<? extends PolicyExecutorTask>>> getTaskMap();
}
