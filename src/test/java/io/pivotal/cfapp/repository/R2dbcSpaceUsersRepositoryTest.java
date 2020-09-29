package io.pivotal.cfapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.pivotal.cfapp.ButlerTest;
import io.pivotal.cfapp.domain.SpaceUsers;
import reactor.test.StepVerifier;

@ButlerTest
@ExtendWith(SpringExtension.class)
public class R2dbcSpaceUsersRepositoryTest {

    private final R2dbcSpaceUsersRepository repo;

    @Autowired
    public R2dbcSpaceUsersRepositoryTest(
            R2dbcSpaceUsersRepository repo
            ) {
        this.repo = repo;
    }

    @BeforeEach
    public void setup() {
        StepVerifier.create(repo.deleteAll()).verifyComplete();
    }

    @Test
    public void testFindByOrgAndSpaceWasSuccessful() {
        SpaceUsers entity = SpaceUsers
                .builder()
                .organization("zoo-labs")
                .space("dev")
                .developers(List.of("devA", "devB", "devC"))
                .build();
        StepVerifier.create(repo.save(entity)
                .then(repo.findByOrganizationAndSpace("zoo-labs", "dev")))
        .assertNext(su -> {
            assertEquals("zoo-labs", su.getOrganization());
            assertEquals("dev", su.getSpace());
            Assertions.assertThat(su.getAuditors()).isEmpty();
            Assertions.assertThat(su.getDevelopers()).containsAll(List.of("deva", "devb", "devc"));
            Assertions.assertThat(su.getManagers()).isEmpty();
        }).verifyComplete();
    }

    @Test
    public void testSaveWasSuccessful() {
        SpaceUsers entity = SpaceUsers
                .builder()
                .organization("zoo-labs")
                .space("dev")
                .auditors(List.of("auditorA"))
                .developers(List.of("devA", "devB", "devC"))
                .managers(List.of("managerA"))
                .build();
        StepVerifier.create(repo.save(entity)
                .thenMany(repo.findAll()))
        .assertNext(su -> {
            assertEquals("zoo-labs", su.getOrganization());
            assertEquals("dev", su.getSpace());
            Assertions.assertThat(su.getAuditors()).containsAll(List.of("auditora"));
            Assertions.assertThat(su.getDevelopers()).containsAll(List.of("deva", "devb", "devc"));
            Assertions.assertThat(su.getManagers()).containsAll(List.of("managera"));
        }).verifyComplete();
    }
}
