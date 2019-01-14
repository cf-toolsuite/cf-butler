package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ServiceInstanceDetailService {

	Mono<ServiceInstanceDetail> save(ServiceInstanceDetail entity);
	Flux<ServiceInstanceDetail> findAll();
	Flux<ServiceInstanceDetail> findByServiceInstancePolicy(ServiceInstancePolicy policy);
	Mono<Void> deleteAll();
}
