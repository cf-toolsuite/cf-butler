package io.pivotal.cfapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.repository.R2dbcAppMetricsRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
public class R2dbcAppMetricsService implements AppMetricsService {

    private final R2dbcAppMetricsRepository repo;

    @Autowired
    public R2dbcAppMetricsService(R2dbcAppMetricsRepository repo) {
        this.repo = repo;
    }

    @Override
    public Flux<Tuple2<String, Long>> byBuildpack() {
        return repo.byBuildpack();
    }

    @Override
    public Flux<Tuple2<String, Long>> byDockerImage() {
        return repo.byDockerImage();
    }

    @Override
    public Flux<Tuple2<String, Long>> byOrganization() {
        return repo.byOrganization();
    }

    @Override
    public Flux<Tuple2<String, Long>> byStack() {
        return repo.byStack();
    }

    @Override
    public Flux<Tuple2<String, Long>> byStatus() {
        return repo.byStatus();
    }

    @Override
    public Mono<Long> totalApplicationInstances() {
        return repo.totalApplicationInstances();
    }

    @Override
    public Mono<Long> totalApplications() {
        return repo.totalApplications();
    }

    @Override
    public Mono<Long> totalCrashedApplicationInstances() {
        return repo.totalCrashedApplicationInstances();
    }

    @Override
    public Mono<Double> totalDiskUsed() {
        return repo.totalDiskUsed();
    }

    @Override
    public Mono<Double> totalMemoryUsed() {
        return repo.totalMemoryUsed();
    }

    @Override
    public Mono<Long> totalRunningApplicationInstances() {
        return repo.totalRunningApplicationInstances();
    }

    @Override
    public Mono<Long> totalStoppedApplicationInstances() {
        return repo.totalStoppedApplicationInstances();
    }

    @Override
    public Flux<Tuple2<String, Long>> totalVelocity() {
        return repo.totalVelocity();
    }

}
