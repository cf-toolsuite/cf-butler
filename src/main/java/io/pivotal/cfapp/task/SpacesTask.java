package io.pivotal.cfapp.task;

import java.util.List;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.Organization;
import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.service.SpaceService;
import reactor.core.publisher.Flux;

@Component
public class SpacesTask implements ApplicationListener<OrganizationsRetrievedEvent> {

	private DefaultCloudFoundryOperations opsClient;
    private SpaceService service;
    private ApplicationEventPublisher publisher;

    @Autowired
    public SpacesTask(
    		DefaultCloudFoundryOperations opsClient,
    		SpaceService service,
    		ApplicationEventPublisher publisher) {
        this.opsClient = opsClient;
        this.service = service;
        this.publisher = publisher;
    }

	@Override
	public void onApplicationEvent(OrganizationsRetrievedEvent event) {
		collect(event.getOrganizations());
	}

	public void collect(List<Organization> organizations) {
        service
            .deleteAll()
			.thenMany(Flux.fromIterable(organizations))
            .flatMap(o -> getSpaces(o))
            .flatMap(service::save)
            .collectList()
            .subscribe(r ->
                publisher.publishEvent(
                    new SpacesRetrievedEvent(this)
                        .spaces(r)
                )
            );
    }

    protected Flux<Space> getSpaces(Organization organization) {
        return DefaultCloudFoundryOperations.builder()
			.from(opsClient)
			.organization(organization.getName())
            .build()
                .spaces()
                    .list()
                    .map(s -> new Space(organization.getName(), s.getName()));
    }

}