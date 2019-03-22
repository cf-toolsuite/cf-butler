package io.pivotal.cfapp.service;

import java.time.LocalDate;

import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface ServiceInstanceDetailService {

	Mono<Void> deleteAll();

	Mono<ServiceInstanceDetail> save(ServiceInstanceDetail entity);

	Flux<ServiceInstanceDetail> findAll();

	Flux<Tuple2<ServiceInstanceDetail, ServiceInstancePolicy>> findByServiceInstancePolicy(ServiceInstancePolicy policy);

	Flux<ServiceInstanceDetail> findByDateRange(LocalDate start, LocalDate end);

}
