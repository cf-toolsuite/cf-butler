package org.cftoolsuite.cfapp.repository;

import org.cftoolsuite.cfapp.ButlerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ButlerTest
public class R2dbcTimeKeeperRepositoryTest {

    private final R2dbcTimeKeeperRepository repo;

    @Autowired
    public R2dbcTimeKeeperRepositoryTest(
            R2dbcTimeKeeperRepository repo
            ) {
        this.repo = repo;
    }

    @BeforeEach
    public void setup() {
        StepVerifier.create(repo.deleteOne()).verifyComplete();
    }

    @Test
    public void testSaveWasSuccessful() {
        LocalDateTime now  = LocalDateTime.now();
        StepVerifier.create(repo.save(now)
                .thenMany(repo.findOne()))
        .assertNext(one -> {
            assertEquals(now, one);
        }).verifyComplete();
    }
}
