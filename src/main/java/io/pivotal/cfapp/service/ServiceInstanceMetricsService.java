package io.pivotal.cfapp.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface ServiceInstanceMetricsService {

    Flux<Tuple2<String, Long>> byOrganization();

    Flux<Tuple2<String, Long>> byService();

    Flux<Tuple2<String, Long>> byServiceAndPlan();

    Mono<Long> totalServiceInstances();

    Flux<Tuple2<String, Long>> totalVelocity();

}
