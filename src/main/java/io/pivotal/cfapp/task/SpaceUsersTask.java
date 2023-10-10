package io.pivotal.cfapp.task;

import java.util.List;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.useradmin.ListSpaceUsersRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.event.SpacesRetrievedEvent;
import io.pivotal.cfapp.service.SpaceUsersService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SpaceUsersTask implements ApplicationListener<SpacesRetrievedEvent> {

    private DefaultCloudFoundryOperations opsClient;
    private SpaceUsersService service;

    @Autowired
    public SpaceUsersTask(
            DefaultCloudFoundryOperations opsClient,
            SpaceUsersService service) {
        this.opsClient = opsClient;
        this.service = service;
    }

    public void collect(List<Space> spaces) {
        log.info("SpaceUsersTask started");
        service
            .deleteAll()
            .thenMany(Flux.fromIterable(spaces))
            .concatMap(this::getSpaceUsers)
            .flatMap(service::save)
            .thenMany(service.findAll())
            .collectList()
            .subscribe(
                result -> {
                    log.info("SpaceUsersTask completed");
                    log.trace("Retrieved {} space user records", result.size());
                },
                error -> {
                    log.error("SpaceUsersTask terminated with error", error);
                }
            );
    }

    protected Mono<SpaceUsers> getSpaceUsers(Space space) {
        log.trace("Fetching space users in org={} and space={}", space.getOrganizationName(), space.getSpaceName());
        return opsClient
                .userAdmin()
                .listSpaceUsers(
                    ListSpaceUsersRequest
                        .builder()
                        .organizationName(space.getOrganizationName())
                        .spaceName(space.getSpaceName())
                        .build()
                )
                .map(su ->
                    SpaceUsers
                        .builder()
                        .organization(space.getOrganizationName())
                        .space(space.getSpaceName())
                        .auditors(su.getAuditors())
                        .managers(su.getManagers())
                        .developers(su.getDevelopers())
                        .build()
                );
    }

    @Override
    public void onApplicationEvent(SpacesRetrievedEvent event) {
        collect(List.copyOf(event.getSpaces()));
    }

}
