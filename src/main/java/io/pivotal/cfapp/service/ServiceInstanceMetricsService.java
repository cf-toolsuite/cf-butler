package io.pivotal.cfapp.service;

import java.util.Map;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

public interface ServiceInstanceMetricsService {

	Flux<Tuple2<String, Long>> byOrganization();

	Flux<Tuple2<String, Long>> byService();

	Flux<Tuple3<String, String, Long>> byServiceAndPlan();

	Mono<Long> totalServiceInstances();

	Flux<Tuple2<String, Long>> totalVelocity();

}