package io.pivotal.cfapp.task;

import java.time.LocalDateTime;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.services.DeleteServiceInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.HistoricalRecord;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.service.HistoricalRecordService;
import io.pivotal.cfapp.service.PoliciesService;
import io.pivotal.cfapp.service.ServiceInstanceDetailService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

@Component
public class ServiceInstancePolicyExecutorTask implements ApplicationRunner {

	private ButlerSettings settings;
	private DefaultCloudFoundryOperations opsClient;
    private ServiceInstanceDetailService serviceInfoService;
    private PoliciesService policiesService;
    private HistoricalRecordService historicalRecordService;

    @Autowired
    public ServiceInstancePolicyExecutorTask(
    		ButlerSettings settings,
    		DefaultCloudFoundryOperations opsClient,
    		ServiceInstanceDetailService serviceInfoService,
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
	    	.filter(
					wl -> wl.getT2().whiteListExists() && 
						wl.getT2().getOrganizationWhiteList().contains(wl.getT1().getOrganization()))
			.map(sid -> sid.getT1())
	    	.filter(bl -> !settings.getOrganizationBlackList().contains(bl.getOrganization()))
	    	.flatMap(ds -> deleteServiceInstance(ds))
	        .flatMap(historicalRecordService::save)
	        .subscribe();
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
        execute();
    }
    
    protected Mono<HistoricalRecord> deleteServiceInstance(ServiceInstanceDetail sd) {
    	return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(sd.getOrganization())
                .space(sd.getSpace())
                .build()
					.services()
						.deleteInstance(DeleteServiceInstanceRequest.builder().name(sd.getName()).build())
						.map(r -> HistoricalRecord
									.builder()
										.dateTimeRemoved(LocalDateTime.now())
										.organization(sd.getOrganization())
										.space(sd.getSpace())
										.id(sd.getServiceId())
										.type("service-instance")
										.name(String.join("::", sd.getName(), sd.getType(), sd.getPlan()))
										.build());			
    }
}
