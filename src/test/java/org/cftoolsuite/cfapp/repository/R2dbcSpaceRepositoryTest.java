package org.cftoolsuite.cfapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.cftoolsuite.cfapp.ButlerTest;
import org.cftoolsuite.cfapp.domain.Space;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import reactor.test.StepVerifier;

@ButlerTest
public class R2dbcSpaceRepositoryTest {

    private final R2dbcSpaceRepository repo;

    @Autowired
    public R2dbcSpaceRepositoryTest(
            R2dbcSpaceRepository repo
            ) {
        this.repo = repo;
    }

    @BeforeEach
    public void setup() {
        StepVerifier.create(repo.deleteAll()).verifyComplete();
    }

    @Test
    public void testSaveWasSuccessful() {
        String organizationId = UUID.randomUUID().toString();
        String spaceId = UUID.randomUUID().toString();
        Space entity = Space
                .builder()
                .spaceId(spaceId)
                .spaceName("dev")
                .organizationId(organizationId)
                .organizationName("zoo-labs")
                .build();
        StepVerifier.create(repo.save(entity)
                .thenMany(repo.findAll()))
        .assertNext(s -> {
            assertEquals(spaceId, s.getSpaceId());
            assertEquals("dev", s.getSpaceName());
            assertEquals(organizationId, s.getOrganizationId());
            assertEquals("zoo-labs", s.getOrganizationName());
        }).verifyComplete();
    }
}
