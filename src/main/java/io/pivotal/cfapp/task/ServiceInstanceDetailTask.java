package io.pivotal.cfapp.task;

import static org.cloudfoundry.util.tuple.TupleUtils.function;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.services.GetServiceInstanceRequest;
import org.cloudfoundry.operations.services.ServiceInstance;
import org.cloudfoundry.operations.services.ServiceInstanceSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.service.ServiceInstanceDetailService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

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

    @Override
    public void onApplicationEvent(SpacesRetrievedEvent event) {
        collect(List.copyOf(event.getSpaces()));
    }

    public void collect(List<Space> spaces) {
        log.info("ServiceInstanceDetailTask started");
    	service
            .deleteAll()
            .thenMany(Flux.fromIterable(spaces))
            .flatMap(space -> buildClient(space))
            .flatMap(client -> getServiceInstanceSummary(client))
            .flatMap(tuple -> getServiceInstanceDetail(tuple))
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

    private Mono<DefaultCloudFoundryOperations> buildClient(Space target) {
        log.trace("Targeting org={} and space={}", target.getOrganization(), target.getSpace());
        return Mono
                .just(DefaultCloudFoundryOperations
                        .builder()
                        .from(opsClient)
                        .organization(target.getOrganization())
		                .space(target.getSpace())
		                .build());
	}

    protected Flux<Tuple2<DefaultCloudFoundryOperations, ServiceInstanceSummary>> getServiceInstanceSummary(DefaultCloudFoundryOperations opsClient) {
        return
            opsClient
                .services()
                    .listInstances()
                    .map(s -> Tuples.of(opsClient, s));
    }

    protected Mono<ServiceInstance> getServiceInstanceDetail(DefaultCloudFoundryOperations opsClient, String serviceName) {
        return opsClient
                .services()
                    .getInstance(GetServiceInstanceRequest.builder().name(serviceName).build());
    }

    protected Mono<ServiceInstanceDetail> getServiceInstanceDetail(
        Tuple2<DefaultCloudFoundryOperations, ServiceInstanceSummary> tuple
    ) {
        DefaultCloudFoundryOperations opsClient = tuple.getT1();
        ServiceInstanceSummary summary = tuple.getT2();
        log.trace("Fetching service instance details for id={}, name={}", summary.getId(), summary.getName());
        return
            Mono.zip(
                Mono.just(opsClient),
                Mono.just(summary),
                getServiceInstanceDetail(opsClient, summary.getName())
            )
            .map(function(this::toServiceInstanceDetail));
    }

    private ServiceInstanceDetail toServiceInstanceDetail(
        DefaultCloudFoundryOperations opsClient, ServiceInstanceSummary summary, ServiceInstance instance
    ) {
        return ServiceInstanceDetail
                .builder()
                    .organization(opsClient.getOrganization())
                    .space(opsClient.getSpace())
                    .serviceInstanceId(summary.getId())
                    .name(summary.getName() != null ? summary.getName(): "user_provided_service")
                    .service(summary.getService())
                    .plan(summary.getPlan())
                    .description(instance.getDescription())
                    .type(instance.getType() != null ? instance.getType().getValue(): "")
                    .applications(summary.getApplications())
                    .lastOperation(instance.getLastOperation())
                    .lastUpdated(StringUtils.isNotBlank(instance.getUpdatedAt()) ? Instant.parse(instance.getUpdatedAt())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime() : null)
                    .dashboardUrl(instance.getDashboardUrl())
                    .requestedState(StringUtils.isNotBlank(instance.getStatus()) ? instance.getStatus().toLowerCase(): "")
                    .build();
    }

}
