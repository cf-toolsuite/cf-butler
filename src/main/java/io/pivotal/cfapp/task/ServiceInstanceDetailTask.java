package io.pivotal.cfapp.task;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.services.GetServiceInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.event.ServiceInstanceDetailRetrievedEvent;
import io.pivotal.cfapp.event.SpacesRetrievedEvent;
import io.pivotal.cfapp.service.ServiceInstanceDetailService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ServiceInstanceDetailTask implements ApplicationListener<SpacesRetrievedEvent> {

    private DefaultCloudFoundryOperations opsClient;
    private ServiceInstanceDetailService service;
    private ApplicationEventPublisher publisher;

    @Autowired
    public ServiceInstanceDetailTask(
            DefaultCloudFoundryOperations opsClient,
            ServiceInstanceDetailService service,
            ApplicationEventPublisher publisher
            ) {
        this.opsClient = opsClient;
        this.service = service;
        this.publisher = publisher;
    }

    private DefaultCloudFoundryOperations buildClient(Space target) {
        return DefaultCloudFoundryOperations
                .builder()
                .from(opsClient)
                .organization(target.getOrganizationName())
                .space(target.getSpaceName())
                .build();
    }

    public void collect(List<Space> spaces) {
        log.info("ServiceInstanceDetailTask started");
        service
            .deleteAll()
            .thenMany(Flux.fromIterable(spaces))
            .concatMap(this::listServiceInstances)
            .flatMap(this::getServiceInstanceDetail)
            .flatMap(service::save)
            .thenMany(service.findAll())
            .collectList()
            .subscribe(
                result -> {
                    publisher.publishEvent(new ServiceInstanceDetailRetrievedEvent(this).detail(result));
                    log.info("ServiceInstanceDetailTask completed");
                },
                error -> {
                    log.error("ServiceInstanceDetailTask terminated with error", error);
                }
            );
    }

    protected Mono<ServiceInstanceDetail> getServiceInstanceDetail(ServiceInstanceDetail fragment) {
        log.trace("Fetching service instance detail for org={}, space={}, id={}, name={}", fragment.getOrganization(), fragment.getSpace(), fragment.getServiceInstanceId(), fragment.getName());
        return buildClient(buildSpace(fragment.getOrganization(), fragment.getSpace()))
                .services()
                .getInstance(GetServiceInstanceRequest.builder().name(fragment.getName()).build())
                .map(sid ->
                    ServiceInstanceDetail
                        .from(fragment)
                        .description(sid.getDescription())
                        .type(sid.getType() != null ? sid.getType().getValue(): null)
                        .lastOperation(sid.getLastOperation())
                        .lastUpdated(StringUtils.isNotBlank(sid.getUpdatedAt()) ? Instant.parse(sid.getUpdatedAt())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime() : null)
                        .dashboardUrl(sid.getDashboardUrl())
                        .requestedState(StringUtils.isNotBlank(sid.getStatus()) ? sid.getStatus().toLowerCase(): null)
                        .build()
                )
                .onErrorResume(e -> Mono.just(fragment));
    }

    protected Flux<ServiceInstanceDetail> listServiceInstances(Space target) {
        return
            buildClient(target)
                .services()
                .listInstances()
                .map(sis ->
                    ServiceInstanceDetail
                        .builder()
                        .serviceInstanceId(sis.getId())
                        .organization(target.getOrganizationName())
                        .space(target.getSpaceName())
                        .name(sis.getName() != null ? sis.getName(): "user_provided_service")
                        .service(sis.getService())
                        .plan(sis.getPlan())
                        .applications(sis.getApplications())
                        .build()
                );
    }

    @Override
    public void onApplicationEvent(SpacesRetrievedEvent event) {
        collect(List.copyOf(event.getSpaces()));
    }

    private static Space buildSpace(String organization, String space) {
        return Space
                .builder()
                .organizationName(organization)
                .spaceName(space)
                .build();
    }
}
