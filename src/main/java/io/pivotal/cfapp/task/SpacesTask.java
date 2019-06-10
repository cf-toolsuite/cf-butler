package io.pivotal.cfapp.task;

import java.sql.SQLException;
import java.util.List;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.Organization;
import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.service.SpaceService;
import io.r2dbc.spi.R2dbcException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
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
		collect(List.copyOf(event.getOrganizations()));
	}

	public void collect(List<Organization> organizations) {
        log.info("SpacesTask started");
        service
            .deleteAll()
			.thenMany(Flux.fromIterable(organizations))
            .flatMap(o -> getSpaces(o))
            .flatMap(service::save)
            .onErrorContinue(R2dbcException.class,
                (ex, data) -> log.error("Problem saving space {}.", data != null ? data.toString(): "<>", ex))
            .onErrorContinue(SQLException.class,
                (ex, data) -> log.error("Problem saving space {}.", data != null ? data.toString(): "<>", ex))
            .thenMany(service.findAll())
                .collectList()
                .subscribe(
                    r -> {
                        publisher.publishEvent(new SpacesRetrievedEvent(this).spaces(r));
                        log.info("SpacesTask completed");
                    }
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