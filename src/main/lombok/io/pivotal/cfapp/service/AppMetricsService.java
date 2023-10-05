package io.pivotal.cfapp.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface AppMetricsService {

    Flux<Tuple2<String, Long>> byBuildpack();

    Flux<Tuple2<String, Long>> byDockerImage();

    Flux<Tuple2<String, Long>> byOrganization();

    Flux<Tuple2<String, Long>> byStack();

    Flux<Tuple2<String, Long>> byStatus();

    Mono<Long> totalApplicationInstances();

    Mono<Long> totalApplications();

    Mono<Long> totalCrashedApplicationInstances();

    Mono<Double> totalDiskUsed();

    Mono<Double> totalMemoryUsed();

    Mono<Long> totalRunningApplicationInstances();

    Mono<Long> totalStoppedApplicationInstances();

    Flux<Tuple2<String, Long>> totalVelocity();

}
