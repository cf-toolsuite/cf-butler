package io.pivotal.cfapp.task;

import java.util.List;

import org.cloudfoundry.client.v2.servicebindings.ListServiceBindingsRequest;
import org.cloudfoundry.client.v3.applications.GetApplicationRequest;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.services.GetServiceInstanceRequest;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.AppRelationship;
import io.pivotal.cfapp.domain.AppRelationshipRequest;
import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.service.AppRelationshipService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class AppRelationshipTask implements ApplicationListener<SpacesRetrievedEvent> {

    private DefaultCloudFoundryOperations opsClient;
    private ReactorCloudFoundryClient cloudFoundryClient;
    private AppRelationshipService service;
    private ApplicationEventPublisher publisher;

    @Autowired
    public AppRelationshipTask(
    		DefaultCloudFoundryOperations opsClient,
    		ReactorCloudFoundryClient cloudFoundryClient,
    		AppRelationshipService service,
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
        log.info("AppRelationshipTask started");
    	service
            .deleteAll()
            .thenMany(Flux.fromIterable(spaces))
            .map(s -> AppRelationshipRequest.builder().organization(s.getOrganization()).space(s.getSpace()).build())
	        .flatMap(serviceSummaryRequest -> getServiceSummary(serviceSummaryRequest))
	        .flatMap(serviceBoundAppIdsRequest -> getServiceBoundApplicationIds(serviceBoundAppIdsRequest))
	        .flatMap(serviceBoundAppNamesRequest -> getServiceBoundApplicationNames(serviceBoundAppNamesRequest))
            .flatMap(appRelationshipRequest -> getAppRelationship(appRelationshipRequest))
            .publishOn(Schedulers.parallel())
            .flatMap(service::save)
            .thenMany(service.findAll().subscribeOn(Schedulers.elastic()))
                .collectList()
                .subscribe(
                    r -> {
                        publisher.publishEvent(new AppRelationshipRetrievedEvent(this).relations(r));
                        log.info("AppRelationshipTask completed");
                    }
                );
    }

    protected Flux<AppRelationshipRequest> getServiceSummary(AppRelationshipRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .space(request.getSpace())
            .build()
                .services()
                    .listInstances()
                    .map(ss -> AppRelationshipRequest.from(request)
                    							.serviceInstanceId(ss.getId())
                    							.serviceName(ss.getName() != null ? ss.getName(): "user_provided_service")
                    							.build());
    }

    protected Mono<AppRelationship> getAppRelationship(AppRelationshipRequest request) {
        return DefaultCloudFoundryOperations.builder()
        	.from(opsClient)
        	.organization(request.getOrganization())
        	.space(request.getSpace())
        	.build()
               .services()
                   .getInstance(GetServiceInstanceRequest.builder().name(request.getServiceName()).build())
                   .map(sd -> AppRelationship
                               .builder()
                                   .organization(request.getOrganization())
                                   .space(request.getSpace())
                                   .appId(request.getApplicationId())
                                   .appName(request.getApplicationName())
                                   .serviceInstanceId(request.getServiceInstanceId())
                                   .serviceName(request.getServiceName())
                                   .servicePlan(sd.getPlan())
                                   .serviceType(sd.getType() != null ? sd.getType().getValue(): "")
                                   .build());
    }

    protected Flux<AppRelationshipRequest> getServiceBoundApplicationIds(AppRelationshipRequest request) {
    	return cloudFoundryClient
			.serviceBindingsV2()
			.list(ListServiceBindingsRequest.builder().serviceInstanceId(request.getServiceInstanceId()).build())
			.flux()
			.flatMap(serviceBindingResponse -> Flux.fromIterable(serviceBindingResponse.getResources()))
			.map(resource -> resource.getEntity())
    		.map(i -> AppRelationshipRequest.from(request).applicationId(i.getApplicationId()).build());
    }

    protected Mono<AppRelationshipRequest> getServiceBoundApplicationNames(AppRelationshipRequest request) {
    	return Mono.just(request.getApplicationId())
    		.flatMap(appId ->
    			cloudFoundryClient
    				.applicationsV3()
    					.get(GetApplicationRequest.builder().applicationId(appId).build())
    					.map(response -> AppRelationshipRequest.from(request).applicationName(response.getName()).build()));
    }

}
