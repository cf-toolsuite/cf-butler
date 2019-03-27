package io.pivotal.cfapp.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface AppMetricsService {

	Flux<Tuple2<String, Long>> byOrganization();

	Flux<Tuple2<String, Long>> byStack();

	Flux<Tuple2<String, Long>> byBuildpack();

	Flux<Tuple2<String, Long>> byDockerImage();

	Flux<Tuple2<String, Long>> byStatus();

	Mono<Long> totalApplications();

	Mono<Long> totalApplicationInstances();

	Mono<Long> totalRunningApplicationInstances();

	Mono<Long> totalStoppedApplicationInstances();

	Mono<Long> totalAnomalousApplicationInstances();

	Mono<Double> totalMemoryUsed();

	Mono<Double> totalDiskUsed();

	Flux<Tuple2<String, Long>> totalVelocity();

}
