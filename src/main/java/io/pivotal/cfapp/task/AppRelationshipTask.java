package io.pivotal.cfapp.task;

import java.util.List;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.pivotal.cfapp.domain.AppRelationship;
import io.pivotal.cfapp.domain.AppRelationshipRequest;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.event.AppRelationshipRetrievedEvent;
import io.pivotal.cfapp.event.ServiceInstanceDetailRetrievedEvent;
import io.pivotal.cfapp.service.AppRelationshipService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AppRelationshipTask implements ApplicationListener<ServiceInstanceDetailRetrievedEvent> {

    private DefaultCloudFoundryOperations opsClient;
    private AppRelationshipService service;
    private ApplicationEventPublisher publisher;

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

    @Override
    public void onApplicationEvent(ServiceInstanceDetailRetrievedEvent event) {
        collect(List.copyOf(event.getDetail()));
    }

    public void collect(List<ServiceInstanceDetail> serviceInstances) {
        log.info("AppRelationshipTask started");
    	service
            .deleteAll()
            .thenMany(Flux.fromIterable(serviceInstances))
            .filter(sid -> !CollectionUtils.isEmpty(sid.getApplications()))
            .flatMap(si -> Flux.fromIterable(AppRelationshipRequest.listOf(si)))
            .flatMap(si -> getAppRelationship(si))
            .flatMap(service::save)
            .thenMany(service.findAll())
                .collectList()
                .subscribe(
                    result -> {
                        publisher.publishEvent(new AppRelationshipRetrievedEvent(this).relations(result));
                        log.info("AppRelationshipTask completed");
                    },
                    error -> {
                        log.error("AppRelationshipTask terminated with error", error);
                    }
                );
    }

    protected Mono<AppRelationship> getAppRelationship(AppRelationshipRequest request) {
        return DefaultCloudFoundryOperations
                .builder()
                    .from(opsClient)
                    .organization(request.getOrganization())
                    .space(request.getSpace())
                    .build()
                    .applications()
                        .list()
                        .filter(as -> as.getName().equals(request.getApplicationName()))
                        .next()
                        .map(as -> AppRelationship
                                    .builder()
                                        .organization(request.getOrganization())
                                        .space(request.getSpace())
                                        .appId(as.getId())
                                        .appName(request.getApplicationName())
                                        .serviceInstanceId(request.getServiceInstanceId())
                                        .serviceName(request.getServiceName())
                                        .servicePlan(request.getServicePlan())
                                        .serviceType(request.getServiceType())
                                        .build());
    }

}
