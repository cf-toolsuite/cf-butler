package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.ApplicationOperation;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.domain.ServiceInstanceOperation;
import reactor.core.publisher.Mono;

public interface PoliciesService {

	Mono<Policies> save(Policies entity);
	Mono<Policies> findApplicationPolicyById(String id);
	Mono<Policies> findServiceInstancePolicyById(String id);
	Mono<Policies> findEndpointPolicyById(String id);
	Mono<Policies> findQueryPolicyById(String id);
	Mono<Policies> findHygienePolicyById(String id);
	Mono<Policies> findLegacyPolicyById(String id);
	Mono<Policies> findAll();
	Mono<Policies> findAllEndpointPolicies();
	Mono<Policies> findAllQueryPolicies();
	Mono<Policies> findAllHygienePolicies();
	Mono<Policies> findAllLegacyPolicies();
	Mono<Void> deleteApplicationPolicyById(String id);
	Mono<Void> deleteServiceInstancePolicyById(String id);
	Mono<Void> deleteEndpointPolicyById(String id);
	Mono<Void> deleteQueryPolicyById(String id);
	Mono<Void> deleteHygienePolicyById(String id);
	Mono<Void> deleteLegacyPolicyById(String id);
	Mono<Void> deleteAll();
	Mono<Policies> findByApplicationOperation(ApplicationOperation operation);
	Mono<Policies> findByServiceInstanceOperation(ServiceInstanceOperation operation);
}
