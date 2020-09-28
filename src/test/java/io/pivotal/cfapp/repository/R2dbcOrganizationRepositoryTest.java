package io.pivotal.cfapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.pivotal.cfapp.ButlerTest;
import io.pivotal.cfapp.domain.Organization;
import reactor.test.StepVerifier;

@ButlerTest
@ExtendWith(SpringExtension.class)
public class R2dbcOrganizationRepositoryTest {

    private final R2dbcOrganizationRepository repo;

    @Autowired
    public R2dbcOrganizationRepositoryTest(
    		R2dbcOrganizationRepository repo
    ) {
        this.repo = repo;
    }

    @BeforeEach
    public void setup() {
        StepVerifier.create(repo.deleteAll()).verifyComplete();
    }

    @Test
    public void testSaveWasSuccessful() {
        String id = UUID.randomUUID().toString();
        String name = "zoo-labs";
        Organization entity = new Organization(id, name);
        StepVerifier.create(repo.save(entity)
            .thenMany(repo.findAll()))
            .assertNext(o -> {
                assertEquals(id, o.getId());
                assertEquals(name, o.getName());
            }).verifyComplete();
    }
}