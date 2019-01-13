package io.pivotal.cfapp.task;

import java.time.LocalDateTime;

import org.cloudfoundry.client.v2.services.DeleteServiceRequest;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.HistoricalRecord;
import io.pivotal.cfapp.domain.ServiceDetail;
import io.pivotal.cfapp.service.HistoricalRecordService;
import io.pivotal.cfapp.service.PoliciesService;
import io.pivotal.cfapp.service.ServiceInfoService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

@Component
public class ServiceInstancePolicyExecutorTask implements ApplicationRunner {

	private ButlerSettings settings;
	private DefaultCloudFoundryOperations opsClient;
    private ServiceInfoService serviceInfoService;
    private PoliciesService policiesService;
    private HistoricalRecordService historicalRecordService;

    @Autowired
    public ServiceInstancePolicyExecutorTask(
    		ButlerSettings settings,
    		DefaultCloudFoundryOperations opsClient,
    		ServiceInfoService serviceInfoService,
    		PoliciesService policiesService,
    		HistoricalRecordService historicalRecordService
    		) {
    	this.settings = settings;
        this.opsClient = opsClient;
        this.serviceInfoService = serviceInfoService;
        this.policiesService = policiesService;
        this.historicalRecordService = historicalRecordService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
    	// do nothing at startup
    }
    
    public void execute() {
    	Hooks.onOperatorDebug();
    	policiesService
	        .findAll()
	        .flux()
	        .flatMap(p -> Flux.fromIterable(p.getServiceInstancePolicies()))
	    	.flatMap(sp -> serviceInfoService.findByServiceInstancePolicy(sp))
	    	.filter(bl -> !settings.getOrganizationBlackList().contains(bl.getOrganization()))
	    	.flatMap(sd -> deleteServiceInstance(sd))
	        .flatMap(historicalRecordService::save)
	        .subscribe();
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
        execute();
    }
    
    protected Mono<HistoricalRecord> deleteServiceInstance(ServiceDetail sd) {
    	return opsClient
			.getCloudFoundryClient()
				.services()
					.delete(DeleteServiceRequest.builder().serviceId(sd.getServiceId()).purge(true).build())
					.map(r -> HistoricalRecord
								.builder()
									.dateTimeRemoved(LocalDateTime.now())
									.organization(sd.getOrganization())
									.space(sd.getSpace())
									.id(sd.getServiceId())
									.type("service-instance")
									.name(String.join("::", sd.getName(), sd.getType(), sd.getPlan()))
									.status(r.getEntity().getStatus())
									.errorDetails(r.getEntity().getErrorDetails() != null ? r.getEntity().getErrorDetails().toString(): null)
									.build());			
    }
}
