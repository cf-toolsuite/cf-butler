package io.pivotal.cfapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppRelationship;
import io.pivotal.cfapp.domain.LegacyPolicy;
import io.pivotal.cfapp.domain.WorkloadsFilter;
import io.pivotal.cfapp.util.PolicyFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LegacyWorkloadsService {

    private final SnapshotService snapshotService;
    private final PolicyFilter filter;

    @Autowired
    public LegacyWorkloadsService(
            SnapshotService snapshotService,
            PolicyFilter filter
            ) {
        this.snapshotService = snapshotService;
        this.filter = filter;
    }

    public Mono<List<AppRelationship>> getLegacyApplicationRelationships(LegacyPolicy policy) {
        return snapshotService
                .assembleSnapshotDetail()
                .flatMapMany(sd -> Flux.fromIterable(sd.getApplicationRelationships()))
                .filter(app -> filter.isWhitelisted(policy, app.getOrganization()))
                .filter(app -> filter.isBlacklisted(app.getOrganization(), app.getSpace()))
                .filter(app -> app.getServiceOffering()!=null ? policy.getServiceOfferings().contains(app.getServiceOffering()):false)
                .collectList();
    }

    public Mono<List<AppRelationship>> getLegacyApplicationRelationships(WorkloadsFilter workloadsFilter) {
        return getLegacyApplicationRelationships(LegacyPolicy.builder().serviceOfferings(workloadsFilter.getServiceOfferings()).build());
    }

    public Mono<List<AppDetail>> getLegacyApplications(LegacyPolicy policy) {
        return snapshotService
                .assembleSnapshotDetail()
                .flatMapMany(sd -> Flux.fromIterable(sd.getApplications()))
                .filter(app -> filter.isWhitelisted(policy, app.getOrganization()))
                .filter(app -> filter.isBlacklisted(app.getOrganization(), app.getSpace()))
                .filter(app -> policy.getStacks().contains(app.getStack()))
                .collectList();
    }

    public Mono<List<AppDetail>> getLegacyApplications(WorkloadsFilter workloadsFilter) {
        return getLegacyApplications(LegacyPolicy.builder().stacks(workloadsFilter.getStacks()).build());
    }   

}
