package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.ServiceDetail;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ServiceInfoService {

	Mono<ServiceDetail> save(ServiceDetail entity);
	Flux<ServiceDetail> findAll();
	Flux<ServiceDetail> findByServiceInstancePolicy(ServiceInstancePolicy policy);
	Mono<Void> deleteAll();
}
