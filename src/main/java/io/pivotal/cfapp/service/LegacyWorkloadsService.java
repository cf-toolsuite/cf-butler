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

    public Mono<List<AppDetail>> getLegacyStackApplications(String list) {
        Set<String> stacks = Set.copyOf(Arrays.asList(list.split("\\s*,\\s*")));
        return getLegacyStackApplications(LegacyPolicy.builder().stacks(stacks).build());
    }

    public Mono<List<AppRelationship>> getLegacyServiceApplications(String list) {
        Set<String> services = Set.copyOf(Arrays.asList(list.split("\\s*,\\s*")));
        return getLegacyServiceApplications(LegacyPolicy.builder().services(services).build());
    }


    public Mono<List<AppDetail>> getLegacyStackApplications(LegacyPolicy policy) {
		    return snapshotService
                .assembleSnapshotDetail()
                .flatMapMany(sd -> Flux.fromIterable(sd.getApplications()))
                .filter(app -> isWhitelisted(policy, app.getOrganization()))
                .filter(app -> isBlacklisted(app.getOrganization()))
                .filter(app -> policy.getStacks().contains(app.getStack()))
                .collectList();
    }

    public Mono<List<AppRelationship>> getLegacyServiceApplications(LegacyPolicy policy) {        
        return snapshotService
            .assembleSnapshotDetail()
            .flatMapMany(sd -> Flux.fromIterable(sd.getApplicationRelationships()))
            .filter(app -> isWhitelisted(policy, app.getOrganization()))
            .filter(app -> isBlacklisted(app.getOrganization()))
            .filter(app -> app.getService()!=null ? policy.getServices().contains(app.getService()):false)
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
