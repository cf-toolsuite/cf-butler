package org.cftoolsuite.cfapp.task;

import org.cftoolsuite.cfapp.domain.Organization;
import org.cftoolsuite.cfapp.event.OrganizationsRetrievedEvent;
import org.cftoolsuite.cfapp.event.TkRetrievedEvent;
import org.cftoolsuite.cfapp.service.OrganizationService;
import org.cloudfoundry.client.v3.organizations.ListOrganizationsRequest;
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
public class OrganizationsTask implements ApplicationListener<TkRetrievedEvent> {

    private final DefaultCloudFoundryOperations opsClient;
    private final OrganizationService organizationService;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public OrganizationsTask(
            DefaultCloudFoundryOperations opsClient,
            OrganizationService organizationService,
            ApplicationEventPublisher publisher) {
        this.opsClient = opsClient;
        this.organizationService = organizationService;
        this.publisher = publisher;
    }

    public void collect() {
        log.info("OrganizationTask started");
        organizationService
            .deleteAll()
            .thenMany(getOrganizations())
            .flatMap(organizationService::save)
            .thenMany(organizationService.findAll())
            .collectList()
            .subscribe(
                result -> {
                    publisher.publishEvent(new OrganizationsRetrievedEvent(this).organizations(result));
                    log.info("OrganizationTask completed. {} organizations found.", result.size());
                },
                error -> {
                    log.error("OrganizationTask terminated with error", error);
                }
            );
    }

    protected Flux<Organization> getOrganizations() {
        return PaginationUtils.requestClientV3Resources(
                page -> opsClient
                .getCloudFoundryClient()
                .organizationsV3()
                .list(ListOrganizationsRequest.builder().page(page).build()))
                .map(os -> new Organization(os.getId(), os.getName()));
    }

    @Override
    public void onApplicationEvent(TkRetrievedEvent event) {
        collect();
    }

}
