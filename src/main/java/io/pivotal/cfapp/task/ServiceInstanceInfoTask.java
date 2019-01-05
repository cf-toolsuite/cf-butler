package io.pivotal.cfapp.task;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

import io.pivotal.cfapp.domain.ServiceDetail;
import io.pivotal.cfapp.domain.ServiceRequest;
import io.pivotal.cfapp.service.ServiceInfoService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ServiceInstanceInfoTask implements ApplicationRunner {

	private static Integer TRUNC_LIMIT = 2500;
    private DefaultCloudFoundryOperations opsClient;
    private ReactorCloudFoundryClient cloudFoundryClient;
    private ServiceInfoService service;
    private ApplicationEventPublisher publisher;

    @Autowired
    public ServiceInstanceInfoTask(
    		DefaultCloudFoundryOperations opsClient,
    		ReactorCloudFoundryClient cloudFoundryClient,
    		ServiceInfoService service,
    		ApplicationEventPublisher publisher
    		) {
        this.opsClient = opsClient;
        this.cloudFoundryClient = cloudFoundryClient;
        this.service = service;
        this.publisher = publisher;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
    	runTask();
    }

    @Scheduled(cron = "${cron}")
    protected void runTask() {
        service
            .deleteAll()
            .thenMany(getOrganizations())
            .flatMap(spaceRequest -> getSpaces(spaceRequest))
            .flatMap(serviceSummaryRequest -> getServiceSummary(serviceSummaryRequest))
            .flatMap(serviceBoundAppIdsRequest -> getServiceBoundApplicationIds(serviceBoundAppIdsRequest))
            .flatMap(serviceBoundAppNamesRequest -> getServiceBoundApplicationNames(serviceBoundAppNamesRequest))
            .flatMap(serviceDetailRequest -> getServiceDetail(serviceDetailRequest))
            .flatMap(service::save)
            .collectList()
            .subscribe(
                r -> publisher.publishEvent(
                    new ServiceInfoRetrievedEvent(this)
                        .detail(r)
            ));
    }

    protected Flux<ServiceRequest> getOrganizations() {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .build()
                .organizations()
                    .list()
                    .map(os -> ServiceRequest.builder().organization(os.getName()).build());
    }

    protected Flux<ServiceRequest> getSpaces(ServiceRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .build()
                .spaces()
                    .list()
                    .map(s -> ServiceRequest.from(request).space(s.getName()).build());
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

    protected Mono<ServiceDetail> getServiceDetail(ServiceRequest request) {
        return DefaultCloudFoundryOperations.builder()
        	.from(opsClient)
        	.organization(request.getOrganization())
        	.space(request.getSpace())
        	.build()
               .services()
                   .getInstance(GetServiceInstanceRequest.builder().name(request.getServiceName()).build())
                   .onErrorResume(e -> Mono.empty())
                   .map(sd -> ServiceDetail
                               .builder()
                                   .organization(request.getOrganization())
                                   .space(request.getSpace())
                                   .name(request.getServiceName())
                                   .service(sd.getService())
                                   .plan(sd.getPlan())
                                   .description(sd.getDescription())
                                   .type(sd.getType() != null ? sd.getType().getValue(): "")
                                   .applications(toTruncatedString(request.getApplicationNames()))
                                   .lastOperation(sd.getLastOperation())
                                   .lastUpdated(StringUtils.isNotBlank(sd.getUpdatedAt()) ? Instant.parse(sd.getUpdatedAt())
                                               .atZone(ZoneId.systemDefault())
                                               .toLocalDateTime() : null)
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
    		.map(i -> ServiceRequest.from(request).applicationIds(i).build());
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
    					.map(n -> ServiceRequest.from(request).applicationNames(n).build());
    }

    private String toTruncatedString(List<String> urls) {
    	String rawData = String.join(",", urls);
    	return rawData.length() <= TRUNC_LIMIT ? rawData : rawData.substring(0, TRUNC_LIMIT);
    }

}
