package io.pivotal.cfapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.pivotal.cfapp.ButlerTest;
import reactor.test.StepVerifier;

@ButlerTest
@ExtendWith(SpringExtension.class)
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
