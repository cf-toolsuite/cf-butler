package org.cftoolsuite.cfapp.repository;

import org.assertj.core.api.Assertions;
import org.cftoolsuite.cfapp.ButlerTest;
import org.cftoolsuite.cfapp.domain.SpaceUsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ButlerTest
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
