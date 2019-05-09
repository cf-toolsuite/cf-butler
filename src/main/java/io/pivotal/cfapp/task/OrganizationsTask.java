package io.pivotal.cfapp.task;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.Organization;
import io.pivotal.cfapp.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class OrganizationsTask implements ApplicationRunner {

    private DefaultCloudFoundryOperations opsClient;
    private OrganizationService service;
    private ApplicationEventPublisher publisher;

    @Autowired
    public OrganizationsTask(
    		DefaultCloudFoundryOperations opsClient,
    		OrganizationService service,
    		ApplicationEventPublisher publisher) {
        this.opsClient = opsClient;
        this.service = service;
        this.publisher = publisher;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        collect();
    }

    public void collect() {
        log.info("OrganizationTask started");
        service
            .deleteAll()
            .thenMany(getOrganizations())
            .publishOn(Schedulers.parallel())
            .flatMap(service::save)
            .thenMany(service.findAll().subscribeOn(Schedulers.elastic()))
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
