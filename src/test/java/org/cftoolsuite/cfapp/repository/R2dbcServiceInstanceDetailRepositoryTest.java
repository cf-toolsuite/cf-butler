package org.cftoolsuite.cfapp.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.cftoolsuite.cfapp.ButlerTest;
import org.cftoolsuite.cfapp.domain.ServiceInstanceDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import reactor.test.StepVerifier;

@ButlerTest
public class R2dbcServiceInstanceDetailRepositoryTest {

    private final R2dbcServiceInstanceDetailRepository detailRepo;

    @Autowired
    public R2dbcServiceInstanceDetailRepositoryTest(
            R2dbcServiceInstanceDetailRepository detailRepo
            ) {
        this.detailRepo = detailRepo;
    }

    @BeforeEach
    public void setup() {
        StepVerifier.create(detailRepo.deleteAll()).verifyComplete();
    }

    @Test
    public void testSaveWasSuccessful() {
        LocalDateTime now = LocalDateTime.now();
        String serviceInstanceId = UUID.randomUUID().toString();
        ServiceInstanceDetail entity = ServiceInstanceDetail
                .builder()
                .serviceInstanceId(serviceInstanceId)
                .name("foo")
                .organization("zoo-labs")
                .space("dev")
                .lastUpdated(now)
                .description("A MySQL database")
                .dashboardUrl("https://foobagger.com/dashboard/fc317c30-0a15-439c-8e46-e36eaa004420")
                .lastOperation("audit.service_instance.create")
                .type("mysql")
                .plan("db-small")
                .service("p.mysql")
                .requestedState("succeeded")
                .build();
        StepVerifier.create(detailRepo.save(entity)
                .thenMany(detailRepo.findAll()))
        .assertNext(sid -> {
            assertNotNull(sid.getPk());
            assertEquals(serviceInstanceId, sid.getServiceInstanceId());
            assertEquals("foo", sid.getName());
            assertEquals("zoo-labs", sid.getOrganization());
            assertEquals("dev", sid.getSpace());
            assertEquals("A MySQL database", sid.getDescription());
            assertEquals("https://foobagger.com/dashboard/fc317c30-0a15-439c-8e46-e36eaa004420", sid.getDashboardUrl());
            assertThat(now).isCloseTo(sid.getLastUpdated(), within(1, ChronoUnit.SECONDS));
            assertEquals("mysql", sid.getType());
            assertEquals("db-small", sid.getPlan());
            assertEquals("p.mysql", sid.getService());
            assertNotNull(sid.getApplications());
            assertTrue(sid.getApplications().isEmpty());
            assertEquals("succeeded", sid.getRequestedState());
        }).verifyComplete();
    }
}
