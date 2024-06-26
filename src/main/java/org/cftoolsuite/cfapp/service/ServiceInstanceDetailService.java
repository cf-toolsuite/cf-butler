package org.cftoolsuite.cfapp.service;

import java.time.LocalDate;

import org.cftoolsuite.cfapp.domain.ServiceInstanceDetail;
import org.cftoolsuite.cfapp.domain.ServiceInstancePolicy;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface ServiceInstanceDetailService {

    Mono<Void> deleteAll();

    Flux<ServiceInstanceDetail> findAll();

    Flux<ServiceInstanceDetail> findByDateRange(LocalDate start, LocalDate end);

    Flux<Tuple2<ServiceInstanceDetail, ServiceInstancePolicy>> findByServiceInstancePolicy(ServiceInstancePolicy policy);

    Mono<ServiceInstanceDetail> save(ServiceInstanceDetail entity);

}
