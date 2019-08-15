package io.pivotal.cfapp.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.HygienePolicy;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DormantWorkloadsService {

    private final EventsService eventsService;
    private final SnapshotService snapshotService;
    private final PasSettings settings;

    @Autowired
    public DormantWorkloadsService(
        EventsService eventsService,
        SnapshotService snapshotService,
        PasSettings settings
    ) {
        this.eventsService = eventsService;
        this.snapshotService = snapshotService;
        this.settings = settings;
    }

    public Mono<List<AppDetail>> getDormantApplications(Integer daysSinceLastUpdate) {
        return getDormantApplications(HygienePolicy.builder().daysSinceLastUpdate(daysSinceLastUpdate).build());
    }

    public Mono<List<ServiceInstanceDetail>> getDormantServiceInstances(Integer daysSinceLastUpdate) {
        return getDormantServiceInstances(HygienePolicy.builder().daysSinceLastUpdate(daysSinceLastUpdate).build());
    }

    public Mono<List<AppDetail>> getDormantApplications(HygienePolicy policy) {
		    return snapshotService
                .assembleSnapshotDetail()
                .flatMapMany(sd -> Flux.fromIterable(sd.getApplications()))
                .filter(app -> isWhitelisted(policy, app.getOrganization()))
                .filter(app -> isBlacklisted(app.getOrganization()))
		        .filter(app -> app.getRequestedState().equalsIgnoreCase("started"))
                // @see https://github.com/reactor/reactor-core/issues/498
                .filterWhen(app -> eventsService.isDormantApplication(app.getAppId(), policy.getDaysSinceLastUpdate()))
                .collectList();
    }

    public Mono<List<ServiceInstanceDetail>> getDormantServiceInstances(HygienePolicy policy) {
		    return snapshotService
                .assembleSnapshotDetail()
                .flatMapMany(sd -> Flux.fromIterable(sd.getServiceInstances()))
                .filter(sid -> isWhitelisted(policy, sid.getOrganization()))
                .filter(sid -> isBlacklisted(sid.getOrganization()))
                // @see https://github.com/reactor/reactor-core/issues/498
                .filterWhen(sid -> eventsService.isDormantServiceInstance(sid.getServiceInstanceId(), policy.getDaysSinceLastUpdate()))
                .collectList();
    }

    private boolean isBlacklisted(String organization) {
        return !settings.getOrganizationBlackList().contains(organization);
    }

    private boolean isWhitelisted(HygienePolicy policy, String organization) {
    	Set<String> prunedSet = new HashSet<>(policy.getOrganizationWhiteList());
    	while (prunedSet.remove(""));
    	Set<String> whitelist =
    			CollectionUtils.isEmpty(prunedSet) ?
    					prunedSet: policy.getOrganizationWhiteList();
    	return
			whitelist.isEmpty() ? true: policy.getOrganizationWhiteList().contains(organization);
	}
}
