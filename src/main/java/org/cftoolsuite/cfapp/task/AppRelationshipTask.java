package org.cftoolsuite.cfapp.task;

import java.util.Arrays;
import java.util.List;

import org.cftoolsuite.cfapp.domain.AppRelationship;
import org.cftoolsuite.cfapp.domain.AppRelationshipRequest;
import org.cftoolsuite.cfapp.domain.ServiceInstanceDetail;
import org.cftoolsuite.cfapp.event.AppRelationshipRetrievedEvent;
import org.cftoolsuite.cfapp.event.ServiceInstanceDetailRetrievedEvent;
import org.cftoolsuite.cfapp.service.AppRelationshipService;
import org.cloudfoundry.client.v3.applications.ApplicationResource;
import org.cloudfoundry.client.v3.applications.ListApplicationsRequest;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.util.PaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AppRelationshipTask implements ApplicationListener<ServiceInstanceDetailRetrievedEvent> {

    private final DefaultCloudFoundryOperations opsClient;
    private final AppRelationshipService service;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public AppRelationshipTask(
            DefaultCloudFoundryOperations opsClient,
            AppRelationshipService service,
            ApplicationEventPublisher publisher
            ) {
        this.opsClient = opsClient;
        this.service = service;
        this.publisher = publisher;
    }

    public void collect(List<ServiceInstanceDetail> serviceInstances) {
        log.info("AppRelationshipTask started");
        service
            .deleteAll()
            .thenMany(Flux.fromIterable(serviceInstances))
            .filter(sid -> !CollectionUtils.isEmpty(sid.getApplications()))
            .flatMap(si -> Flux.fromIterable(AppRelationshipRequest.listOf(si)))
            .flatMap(this::getAppRelationship)
            .flatMap(service::save)
            .thenMany(service.findAll())
            .collectList()
            .subscribe(
                result -> {
                    publisher.publishEvent(new AppRelationshipRetrievedEvent(this).relations(result));
                    log.info("AppRelationshipTask completed. {} application to service instance binding relationships found.", result.size());
                },
                error -> {
                    log.error("AppRelationshipTask terminated with error", error);
                }
            );
    }

    protected Mono<AppRelationship> getAppRelationship(AppRelationshipRequest request) {
        Flux<ApplicationResource> resources =
                PaginationUtils.requestClientV3Resources(
                        page -> DefaultCloudFoundryOperations
                        .builder()
                        .from(opsClient)
                        .organization(request.getOrganization())
                        .space(request.getSpace())
                        .build()
                        .getCloudFoundryClient()
                        .applicationsV3()
                        .list(ListApplicationsRequest.builder().page(page).addAllNames(Arrays.asList(new String[] { request.getApplicationName() })).build()));
        return resources
                .next()
                .map(ar ->
                    AppRelationship
                        .builder()
                        .organization(request.getOrganization())
                        .space(request.getSpace())
                        .appId(ar.getId())
                        .appName(request.getApplicationName())
                        .serviceInstanceId(request.getServiceInstanceId())
                        .serviceName(request.getServiceName())
                        .serviceOffering(request.getServiceOffering())
                        .servicePlan(request.getServicePlan())
                        .serviceType(request.getServiceType())
                        .build());
    }

    @Override
    public void onApplicationEvent(ServiceInstanceDetailRetrievedEvent event) {
        collect(List.copyOf(event.getDetail()));
    }

}
