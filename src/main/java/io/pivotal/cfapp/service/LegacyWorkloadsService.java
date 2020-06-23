package io.pivotal.cfapp.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppRelationship;
import io.pivotal.cfapp.domain.LegacyPolicy;
import io.pivotal.cfapp.domain.WorkloadsFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LegacyWorkloadsService {

    private final SnapshotService snapshotService;
    private final PasSettings settings;

    @Autowired
    public LegacyWorkloadsService(
        SnapshotService snapshotService,
        PasSettings settings
    ) {
        this.snapshotService = snapshotService;
        this.settings = settings;
    }

    public Mono<List<AppDetail>> getLegacyApplications(WorkloadsFilter workloadsFilter) {
        return getLegacyApplications(LegacyPolicy.builder().stacks(workloadsFilter.getStacks()).build());
    }

    public Mono<List<AppRelationship>> getLegacyApplicationRelationships(WorkloadsFilter workloadsFilter) {
        return getLegacyApplicationRelationships(LegacyPolicy.builder().serviceOfferings(workloadsFilter.getServiceOfferings()).build());
    }

    public Mono<List<AppDetail>> getLegacyApplications(LegacyPolicy policy) {
		    return snapshotService
                .assembleSnapshotDetail()
                .flatMapMany(sd -> Flux.fromIterable(sd.getApplications()))
                .filter(app -> isWhitelisted(policy, app.getOrganization()))
                .filter(app -> isBlacklisted(app.getOrganization()))
                .filter(app -> policy.getStacks().contains(app.getStack()))
                .collectList();
    }

    public Mono<List<AppRelationship>> getLegacyApplicationRelationships(LegacyPolicy policy) {        
        return snapshotService
            .assembleSnapshotDetail()
            .flatMapMany(sd -> Flux.fromIterable(sd.getApplicationRelationships()))
            .filter(app -> isWhitelisted(policy, app.getOrganization()))
            .filter(app -> isBlacklisted(app.getOrganization()))
            .filter(app -> app.getServiceOffering()!=null ? policy.getServiceOfferings().contains(app.getServiceOffering()):false)
            .collectList();
}

    private boolean isBlacklisted(String organization) {
        return !settings.getOrganizationBlackList().contains(organization);
    }

    private boolean isWhitelisted(LegacyPolicy policy, String organization) {
    	Set<String> prunedSet = new HashSet<>(policy.getOrganizationWhiteList());
    	while (prunedSet.remove(""));
    	Set<String> whitelist =
    			CollectionUtils.isEmpty(prunedSet) ?
    					prunedSet: policy.getOrganizationWhiteList();
    	return
			whitelist.isEmpty() ? true: policy.getOrganizationWhiteList().contains(organization);
    }

}
