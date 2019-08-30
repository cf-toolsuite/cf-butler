package io.pivotal.cfapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.pivotal.cfapp.ButlerTest;
import io.pivotal.cfapp.domain.AppDetail;
import reactor.test.StepVerifier;

@ButlerTest
@ExtendWith(SpringExtension.class)
public class R2dbcAppDetailRepositoryTest {

    private final R2dbcAppDetailRepository detailRepo;

    @Autowired
    public R2dbcAppDetailRepositoryTest(
        R2dbcAppDetailRepository detailRepo
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
        String appId = UUID.randomUUID().toString();
        AppDetail entity = AppDetail
                            .builder()
                                .appId(appId)
                                .appName("foo")
                                .organization("zoo-labs")
                                .space("dev")
                                .lastPushed(now)
                                .stack("cflinuxfs3")
                                .buildpack("java")
                                .runningInstances(1)
                                .totalInstances(1)
                                .memoryUsage(1L)
                                .diskUsage(1L)
                                .requestedState("stopped")
                                .build();
        StepVerifier.create(detailRepo.save(entity)
            .thenMany(detailRepo.findAll()))
            .assertNext(ad -> {
                assertNotNull(ad.getPk());
                assertEquals(appId, ad.getAppId());
                assertEquals("foo", ad.getAppName());
                assertEquals("zoo-labs", ad.getOrganization());
                assertEquals("dev", ad.getSpace());
                assertEquals(now, ad.getLastPushed());
                assertNull(ad.getBuildpackVersion());
                assertEquals("java", ad.getBuildpack());
                assertEquals("cflinuxfs3", ad.getStack());
                assertEquals(1, ad.getRunningInstances());
                assertEquals(1, ad.getTotalInstances());
                assertEquals(1L, ad.getMemoryUsage());
                assertEquals(1L, ad.getDiskUsage());
                assertNotNull(ad.getUrls());
                assertTrue(ad.getUrls().isEmpty());
                assertNull(ad.getLastEvent());
                assertNull(ad.getLastEventActor());
                assertNull(ad.getLastEventTime());
                assertEquals("stopped", ad.getRequestedState());
            }).verifyComplete();
    }
}