package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.ApplicationOperation;
import io.pivotal.cfapp.domain.Policies;
import io.pivotal.cfapp.domain.ServiceInstanceOperation;
import reactor.core.publisher.Mono;

public interface PoliciesService {

	Mono<Policies> save(Policies entity);
	Mono<Policies> findApplicationPolicyById(String id);
	Mono<Policies> findServiceInstancePolicyById(String id);
	Mono<Policies> findQueryPolicyById(String id);
	Mono<Policies> findAll();
	Mono<Policies> findAllQueryPolicies();
	Mono<Void> deleteApplicationPolicyById(String id);
	Mono<Void> deleteServiceInstancePolicyById(String id);
	Mono<Void> deleteQueryPolicyById(String id);
	Mono<Void> deleteAll();
	Mono<Policies> findByApplicationOperation(ApplicationOperation operation);
	Mono<Policies> findByServiceInstanceOperation(ServiceInstanceOperation operation);
}
