package org.cftoolsuite.cfapp.task;

import java.util.List;

import org.cftoolsuite.cfapp.domain.Organization;
import org.cftoolsuite.cfapp.domain.Space;
import org.cftoolsuite.cfapp.event.OrganizationsRetrievedEvent;
import org.cftoolsuite.cfapp.event.SpacesRetrievedEvent;
import org.cftoolsuite.cfapp.service.SpaceService;
import org.cloudfoundry.client.v3.spaces.ListSpacesRequest;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.util.PaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class SpacesTask implements ApplicationListener<OrganizationsRetrievedEvent> {

    private final DefaultCloudFoundryOperations opsClient;
    private final SpaceService service;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public SpacesTask(
            DefaultCloudFoundryOperations opsClient,
            SpaceService service,
            ApplicationEventPublisher publisher) {
        this.opsClient = opsClient;
        this.service = service;
        this.publisher = publisher;
    }

    public void collect(List<Organization> organizations) {
        log.info("SpacesTask started");
        service
            .deleteAll()
            .thenMany(Flux.fromIterable(organizations))
            .flatMap(this::getSpaces)
            .flatMap(service::save)
            .thenMany(service.findAll())
            .collectList()
            .subscribe(
                result -> {
                    publisher.publishEvent(new SpacesRetrievedEvent(this).spaces(result));
                    log.info("SpacesTask completed. {} spaces found.", result.size());
                },
                error -> {
                    log.error("SpacesTask terminated with error", error);
                }
            );
    }

    protected Flux<Space> getSpaces(Organization organization) {
        return PaginationUtils.requestClientV3Resources(
                page ->
                    opsClient
                        .getCloudFoundryClient()
                        .spacesV3()
                        .list(ListSpacesRequest.builder().page(page).organizationIds(new String[] { organization.getId() }).build()))
                        .map(response ->
                            Space
                                .builder()
                                .organizationId(organization.getId())
                                .organizationName(organization.getName())
                                .spaceId(response.getId())
                                .spaceName(response.getName())
                                .build()
                        );
    }

    @Override
    public void onApplicationEvent(OrganizationsRetrievedEvent event) {
        collect(List.copyOf(event.getOrganizations()));
    }

}
