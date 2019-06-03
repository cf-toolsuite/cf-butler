package io.pivotal.cfapp.task;

import java.util.stream.Collectors;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.Organization;
import io.pivotal.cfapp.service.OrganizationService;
import io.r2dbc.spi.R2dbcException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

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
            .collect(Collectors.toSet())
            .flatMapMany(s -> Flux.fromIterable(s))
            .publishOn(Schedulers.parallel())
            .flatMap(organizationService::save)
            .onErrorContinue(R2dbcException.class,
                (ex, data) -> log.error("Problem saving organization {}.", data != null ? data.toString(): "<>", ex))
            .thenMany(organizationService.findAll().subscribeOn(Schedulers.elastic()))
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
