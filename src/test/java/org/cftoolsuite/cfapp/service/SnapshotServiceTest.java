package org.cftoolsuite.cfapp.service;

import org.cftoolsuite.cfapp.ButlerTest;
import org.cftoolsuite.cfapp.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ButlerTest
public class SnapshotServiceTest {

    private final SnapshotService snapService;
    private final AppDetailService appService;
    private final ServiceInstanceDetailService siService;
    private final SpaceUsersService usersService;

    @Autowired
    public SnapshotServiceTest(
            SnapshotService snapService,
            AppDetailService appService,
            ServiceInstanceDetailService siService,
            SpaceUsersService usersService
            ) {
        this.snapService = snapService;
        this.appService = appService;
        this.siService = siService;
        this.usersService = usersService;
    }

    @BeforeEach
    public void setUp() {
        AppDetail ad = AppDetail
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
        ServiceInstanceDetail sid = ServiceInstanceDetail
                .builder()
                .serviceInstanceId("bar-id")
                .name("bar")
                .service("MySQL")
                .description("The big kahuna")
                .applications(Arrays.asList(new String[] { "foo" }))
                .lastUpdated(LocalDateTime.now())
                .lastOperation("created")
                .plan("large")
                .requestedState("created")
                .space("dev")
                .organization("zoo-labs")
                .type("managed_service_instance")
                .build();
        SpaceUsers su = SpaceUsers
                .builder()
                .auditors(Arrays.asList(new String[] { "marty@mcfly.org" }))
                .developers(Arrays.asList(new String[] { "bruce@wayneenterprises.com" }))
                .managers(Arrays.asList(new String[] { "nickfury@avengers.com", "NickFury@avengers.com" }))
                .organization("zoo-labs")
                .space("dev")
                .build();
        StepVerifier.create(appService.deleteAll().then(appService.save(ad))).expectNext(ad).verifyComplete();
        StepVerifier.create(siService.deleteAll().then(siService.save(sid))).expectNext(sid).verifyComplete();
        StepVerifier.create(usersService.deleteAll().then(usersService.save(su))).expectNext(su).verifyComplete();
    }

    @Test
    public void testAssembleApplicationCounts() {
        Mono<ApplicationCounts> input = snapService.assembleApplicationCounts();
        StepVerifier.create(input)
        .assertNext(ac -> {
            assertEquals(1, ac.getByBuildpack().get("java_buildpack"));
            assertEquals(1, ac.getByOrganization().get("zoo-labs"));
            assertEquals(1, ac.getByStack().get("cflinuxfs3"));
            assertEquals(0, ac.getByDockerImage().get("--"));
            assertEquals(1, ac.getByStatus().get("stopped"));
        }).verifyComplete();
    }

    @Test
    public void testAssembleServiceInstanceCounts() {
        Mono<ServiceInstanceCounts> input = snapService.assembleServiceInstanceCounts();
        StepVerifier.create(input)
        .assertNext(ac -> {
            assertEquals(1L, ac.getByOrganization().get("zoo-labs"));
            assertEquals(1L, ac.getByService().get("MySQL"));
            assertEquals(1L, ac.getByServiceAndPlan().get("MySQL/large"));
        }).verifyComplete();
    }

    @Test
    public void testAssembleUserCounts() {
        Mono<UserCounts> input = snapService.assembleUserCounts();
        StepVerifier
        .create(input)
        .assertNext(uc -> {
            assertEquals(3, uc.getTotalUserAccounts());
            assertEquals(0, uc.getTotalServiceAccounts());
        })
        .verifyComplete();
    }

}
