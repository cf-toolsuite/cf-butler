package io.pivotal.cfapp.task;

import java.util.List;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.useradmin.ListSpaceUsersRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.domain.UserRequest;
import io.pivotal.cfapp.service.SpaceUsersService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

    @Override
    public void onApplicationEvent(SpacesRetrievedEvent event) {
        collect(List.copyOf(event.getSpaces()));
    }

    public void collect(List<Space> spaces) {
        log.info("SpaceUsersTask started");
    	service
            .deleteAll()
            .thenMany(Flux.fromIterable(spaces))
            .map(s -> UserRequest.builder().organization(s.getOrganization()).spaceName(s.getSpace()).build())
            .flatMap(spaceUsersRequest -> getSpaceUsers(spaceUsersRequest))
            .publishOn(Schedulers.parallel())
            .flatMap(service::save)
                .collectList()
                .subscribe(e -> log.info("SpaceUsersTask completed"));
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
