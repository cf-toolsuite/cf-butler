package org.cftoolsuite.cfapp.repository;

import org.cftoolsuite.cfapp.ButlerTest;
import org.cftoolsuite.cfapp.domain.AppDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ButlerTest
public class R2dbcAppMetricsRepositoryTest {

    private final R2dbcAppMetricsRepository metricsRepo;
    private final R2dbcAppDetailRepository detailRepo;

    @Autowired
    public R2dbcAppMetricsRepositoryTest(
            R2dbcAppMetricsRepository metricsRepo,
            R2dbcAppDetailRepository detailRepo
            ) {
        this.metricsRepo = metricsRepo;
        this.detailRepo = detailRepo;
    }

    @BeforeEach
    public void setUp() {
        AppDetail entity = AppDetail
                .builder()
                .appId("foo-id")
                .appName("foo")
                .organization("zoo-labs")
                .space("dev")
                .lastPushed(LocalDateTime.now())
                .stack("cflinuxfs3")
                .buildpack("java_buildpack")
                .runningInstances(1)
                .totalInstances(1)
                .memoryUsed(1L)
                .diskUsed(1L)
                .requestedState("stopped")
                .build();
        StepVerifier.create(detailRepo.deleteAll().then(detailRepo.save(entity))).expectNext(entity).verifyComplete();
    }

    @Test
    public void testByBuildpack() {
        Flux<Tuple2<String, Long>> input = metricsRepo.byBuildpack();
        StepVerifier.create(input)
        .assertNext(m -> {
            assertEquals(1L, m.getT2());
            assertEquals("java_buildpack", m.getT1());
        }).verifyComplete();
    }

    @Test
    public void testByDockerImage() {
        Flux<Tuple2<String, Long>> input = metricsRepo.byDockerImage();
        StepVerifier.create(input)
        .assertNext(m -> {
            assertEquals(0L, m.getT2());
            assertEquals("--", m.getT1());
        }).verifyComplete();
    }

    @Test
    public void testByOrganization() {
        Flux<Tuple2<String, Long>> input = metricsRepo.byOrganization();
        StepVerifier.create(input)
        .assertNext(m -> {
            assertEquals(1L, m.getT2());
            assertEquals("zoo-labs", m.getT1());
        }).verifyComplete();
    }

    @Test
    public void testByStack() {
        Flux<Tuple2<String, Long>> input = metricsRepo.byStack();
        StepVerifier.create(input)
        .assertNext(m -> {
            assertEquals(1L, m.getT2());
            assertEquals("cflinuxfs3", m.getT1());
        }).verifyComplete();
    }

    @Test
    public void testCountByDateRange() {
        final LocalDate now = LocalDate.now();
        Mono<Long> input = metricsRepo.countByDateRange(now.minusDays(1), now);
        StepVerifier.create(input).assertNext(r -> assertEquals(1L, r)).verifyComplete();
    }

    @Test
    public void testCountStagnant() {
        final LocalDate now = LocalDate.now();
        Mono<Long> input = metricsRepo.countStagnant(now.minusYears(1));
        StepVerifier.create(input).assertNext(r -> assertEquals(0L, r)).verifyComplete();
    }

    @Test
    public void testTotalApplications() {
        Mono<Long> input = metricsRepo.totalApplications();
        StepVerifier.create(input).assertNext(r -> assertEquals(1L, r)).verifyComplete();
    }

    @Test
    public void testTotalRunningApplications() {
        Mono<Long> input = metricsRepo.totalRunningApplicationInstances();
        StepVerifier.create(input).assertNext(r -> assertEquals(0L, r)).verifyComplete();
    }

    @Test
    public void testTotalStoppedApplications() {
        Mono<Long> input = metricsRepo.totalStoppedApplicationInstances();
        StepVerifier.create(input).assertNext(r -> assertEquals(1L, r)).verifyComplete();
    }

    @Test
    public void testTotalVelocity() {
        Flux<Tuple2<String, Long>> input = metricsRepo.totalVelocity();
        StepVerifier.create(input).expectNextCount(9L).verifyComplete();
    }
}
