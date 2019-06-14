package io.pivotal.cfapp.task;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.Organization;
import io.pivotal.cfapp.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class OrganizationsTask implements ApplicationListener<TkRetrievedEvent> {

    private final DefaultCloudFoundryOperations opsClient;
    private final OrganizationService organizationService;
    private ApplicationEventPublisher publisher;

    @Autowired
    public OrganizationsTask(
    		DefaultCloudFoundryOperations opsClient,
            OrganizationService organizationService,
    		ApplicationEventPublisher publisher) {
        this.opsClient = opsClient;
        this.organizationService = organizationService;
        this.publisher = publisher;
    }

    @Override
    public void onApplicationEvent(TkRetrievedEvent event) {
        collect();
    }

    public void collect() {
        log.info("OrganizationTask started");
        organizationService
            .deleteAll()
            .thenMany(getOrganizations())
            .distinct()
            .flatMap(organizationService::save)
            .thenMany(organizationService.findAll())
                .collectList()
                .subscribe(
                    r -> {
                        publisher.publishEvent(new OrganizationsRetrievedEvent(this).organizations(r));
                        log.info("OrganizationTask completed");
                    }
                );
    }

    @Scheduled(cron = "${cron.collection}")
    protected void runTask() {
        collect();
    }

    protected Flux<Organization> getOrganizations() {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .build()
                .organizations()
                    .list()
                    .map(os -> new Organization(os.getId(), os.getName()));
    }

}
