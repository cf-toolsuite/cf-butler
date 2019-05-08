package io.pivotal.cfapp.task;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.client.v2.servicebindings.ListServiceBindingsRequest;
import org.cloudfoundry.client.v3.applications.GetApplicationRequest;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.services.GetServiceInstanceRequest;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.ServiceRequest;
import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.service.ServiceInstanceDetailService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class ServiceInstanceDetailTask implements ApplicationListener<SpacesRetrievedEvent> {

    private DefaultCloudFoundryOperations opsClient;
    private ReactorCloudFoundryClient cloudFoundryClient;
    private ServiceInstanceDetailService service;
    private ApplicationEventPublisher publisher;

    @Autowired
    public ServiceInstanceDetailTask(
    		DefaultCloudFoundryOperations opsClient,
    		ReactorCloudFoundryClient cloudFoundryClient,
    		ServiceInstanceDetailService service,
    		ApplicationEventPublisher publisher
    		) {
        this.opsClient = opsClient;
        this.cloudFoundryClient = cloudFoundryClient;
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
            .map(s -> ServiceRequest.builder().organization(s.getOrganization()).space(s.getSpace()).build())
	        .flatMap(serviceSummaryRequest -> getServiceSummary(serviceSummaryRequest))
	        .flatMap(serviceBoundAppIdsRequest -> getServiceBoundApplicationIds(serviceBoundAppIdsRequest))
	        .flatMap(serviceBoundAppNamesRequest -> getServiceBoundApplicationNames(serviceBoundAppNamesRequest))
            .flatMap(serviceDetailRequest -> getServiceDetail(serviceDetailRequest))
            .publishOn(Schedulers.parallel())
            .flatMap(service::save)
            .onErrorContinue(
                (ex, data) -> log.error("Problem saving service instance {}.", data != null ? data.toString(): "<>", ex))
            .thenMany(service.findAll().subscribeOn(Schedulers.elastic()))
                .collectList()
                .subscribe(
                    r -> {
                        publisher.publishEvent(new ServiceInstanceDetailRetrievedEvent(this).detail(r));
                        log.info("ServiceInstanceDetailTask completed");
                    }
                );
    }

    protected Flux<ServiceRequest> getServiceSummary(ServiceRequest request) {
        return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(request.getOrganization())
                .space(request.getSpace())
                .build()
                    .services()
                        .listInstances()
                        .map(ss -> ServiceRequest.from(request)
                                                    .id(ss.getId())
                                                    .serviceName(ss.getName() != null ? ss.getName(): "user_provided_service")
                                                    .build());
    }

    protected Mono<ServiceInstanceDetail> getServiceDetail(ServiceRequest request) {
        return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(request.getOrganization())
                .space(request.getSpace())
                .build()
                .services()
                    .getInstance(GetServiceInstanceRequest.builder().name(request.getServiceName()).build())
                    .onErrorContinue(
                            Exception.class,
                            (ex, data) -> log.error("Trouble fetching service instance details {}.", data != null ? data.toString(): "<>", ex))
                    .map(sd -> ServiceInstanceDetail
                                .builder()
                                    .organization(request.getOrganization())
                                    .space(request.getSpace())
                                    .serviceInstanceId(request.getId())
                                    .name(sd.getName() != null ? sd.getName(): "user_provided_service")
                                    .service(sd.getService())
                                    .plan(sd.getPlan())
                                    .description(sd.getDescription())
                                    .type(sd.getType() != null ? sd.getType().getValue(): "")
                                    .applications(request.getApplicationNames())
                                    .lastOperation(sd.getLastOperation())
                                    .lastUpdated(StringUtils.isNotBlank(sd.getUpdatedAt()) ? Instant.parse(sd.getUpdatedAt())
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDateTime() : LocalDateTime.MIN)
                                    .dashboardUrl(sd.getDashboardUrl())
                                    .requestedState(StringUtils.isNotBlank(sd.getUpdatedAt()) ? sd.getStatus().toLowerCase(): "")
                                    .build());
    }

    protected Mono<ServiceRequest> getServiceBoundApplicationIds(ServiceRequest request) {
    	return cloudFoundryClient
                .serviceBindingsV2()
                    .list(ListServiceBindingsRequest.builder().serviceInstanceId(request.getId()).build())
                    .flux()
                    .flatMap(serviceBindingResponse -> Flux.fromIterable(serviceBindingResponse.getResources()))
                    .map(resource -> resource.getEntity())
                    .map(entity -> entity.getApplicationId())
                    .collectList()
                    .map(ids -> ServiceRequest.from(request).applicationIds(ids).build());
    }

    protected Mono<ServiceRequest> getServiceBoundApplicationNames(ServiceRequest request) {
    	return Flux
    		.fromIterable(request.getApplicationIds())
    		.flatMap(appId ->
    			cloudFoundryClient
    				.applicationsV3()
    					.get(GetApplicationRequest.builder().applicationId(appId).build())
    					.map(response -> response.getName()))
    					.collectList()
    					.map(names -> ServiceRequest.from(request).applicationNames(names).build());
    }

}
