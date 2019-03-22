package io.pivotal.cfapp.task;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.useradmin.ListSpaceUsersRequest;
import org.cloudfoundry.operations.util.OperationsLogging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.UserRequest;
import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.service.SpaceUsersService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

@Component
public class SpaceUsersTask implements ApplicationRunner {

    private DefaultCloudFoundryOperations opsClient;
    private SpaceUsersService service;

    @Autowired
    public SpaceUsersTask(
    		DefaultCloudFoundryOperations opsClient,
    		SpaceUsersService service) {
        this.opsClient = opsClient;
        this.service = service;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        collect();
    }


    protected void collect() {
        Hooks.onOperatorDebug();
        service
            .deleteAll()
            .thenMany(getOrganizations())
            .flatMap(spaceRequest -> getSpaces(spaceRequest))
            .flatMap(spaceUsersRequest -> getSpaceUsers(spaceUsersRequest))
            .flatMap(service::save)
            .subscribe();
    }

    @Scheduled(cron = "${cron.collection}")
    protected void runTask() {
        collect();
    }

    protected Flux<UserRequest> getOrganizations() {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .build()
                .organizations()
                    .list()
                    .map(os -> UserRequest.builder().organization(os.getName()).build());
    }

    protected Flux<UserRequest> getSpaces(UserRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .build()
                .spaces()
                    .list()
                    .map(ss -> UserRequest.from(request).spaceName(ss.getName()).build());
    }

    protected Mono<SpaceUsers> getSpaceUsers(UserRequest request) {
        return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(request.getOrganization())
                .space(request.getSpaceName())
                .build()
                	.userAdmin()
                		.listSpaceUsers(
                				ListSpaceUsersRequest
                					.builder()
                						.organizationName(request.getOrganization())
                						.spaceName(request.getSpaceName()).build()
                        )
                        .flux()
                        .onErrorContinue(
                            NullPointerException.class,
                            (ex, data) -> OperationsLogging.log("Could not obtain subset of users in space. " + ex.getMessage()))
                        .next()
                		.map(su -> SpaceUsers
                						.builder()
                							.organization(request.getOrganization())
                							.space(request.getSpaceName())
                							.auditors(su.getAuditors())
                							.managers(su.getManagers())
                							.developers(su.getDevelopers())
                							.build()
                        );
    }

}
