package io.pivotal.cfapp.task;

import org.cloudfoundry.client.v2.servicebindings.ListServiceBindingsRequest;
import org.cloudfoundry.client.v3.applications.GetApplicationRequest;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.services.GetServiceInstanceRequest;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.AppRelationship;
import io.pivotal.cfapp.domain.AppRelationshipRequest;
import io.pivotal.cfapp.service.AppRelationshipService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

@Component
public class AppRelationshipTask implements ApplicationRunner {

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
    public void run(ApplicationArguments args) throws Exception {
    	collect();
    }

    public void collect() {
    	Hooks.onOperatorDebug();
    	service
	        .deleteAll()
	        .thenMany(getOrganizations())
	        .flatMap(spaceRequest -> getSpaces(spaceRequest))
	        .flatMap(serviceSummaryRequest -> getServiceSummary(serviceSummaryRequest))
	        .flatMap(serviceBoundAppIdsRequest -> getServiceBoundApplicationIds(serviceBoundAppIdsRequest))
	        .flatMap(serviceBoundAppNamesRequest -> getServiceBoundApplicationNames(serviceBoundAppNamesRequest))
	        .flatMap(appRelationshipRequest -> getAppRelationship(appRelationshipRequest))
	        .flatMap(service::save)
	        .collectList()
	        .subscribe(
	            r -> publisher.publishEvent(
	                new AppRelationshipRetrievedEvent(this)
	                    .relations(r)
	        ));
    }

    @Scheduled(cron = "${cron.collection}")
    protected void runTask() {
        collect();
    }

	protected Flux<AppRelationshipRequest> getOrganizations() {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .build()
                .organizations()
                    .list()
                    .map(os -> AppRelationshipRequest.builder().organization(os.getName()).build());
    }

    protected Flux<AppRelationshipRequest> getSpaces(AppRelationshipRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .build()
                .spaces()
                    .list()
                    .map(s -> AppRelationshipRequest.from(request).space(s.getName()).build());
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
                    							.serviceId(ss.getId())
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
                   .onErrorResume(e -> Mono.empty())
                   .map(sd -> AppRelationship
                               .builder()
                                   .organization(request.getOrganization())
                                   .space(request.getSpace())
                                   .appId(request.getApplicationId())
                                   .appName(request.getApplicationName())
                                   .serviceId(request.getServiceId())
                                   .serviceName(request.getServiceName())
                                   .servicePlan(sd.getPlan())
                                   .serviceType(sd.getType() != null ? sd.getType().getValue(): "")
                                   .build());
    }

    protected Flux<AppRelationshipRequest> getServiceBoundApplicationIds(AppRelationshipRequest request) {
    	return cloudFoundryClient
			.serviceBindingsV2()
			.list(ListServiceBindingsRequest.builder().serviceInstanceId(request.getServiceId()).build())
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
