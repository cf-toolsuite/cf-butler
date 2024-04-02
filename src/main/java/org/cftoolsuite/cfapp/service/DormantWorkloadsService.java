package org.cftoolsuite.cfapp.service;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.domain.AppRelationship;
import org.cftoolsuite.cfapp.domain.HygienePolicy;
import org.cftoolsuite.cfapp.domain.ServiceInstanceDetail;
import org.cftoolsuite.cfapp.util.PolicyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DormantWorkloadsService {

    private final EventsService eventsService;
    private final SnapshotService snapshotService;
    private final AppDetailService appDetailService;
    private final AppRelationshipService relationshipService;
    private final PolicyFilter filter;

    @Autowired
    public DormantWorkloadsService(
            EventsService eventsService,
            SnapshotService snapshotService,
            AppDetailService appDetailService,
            AppRelationshipService relationshipService,
            PolicyFilter filter
            ) {
        this.eventsService = eventsService;
        this.snapshotService = snapshotService;
        this.appDetailService = appDetailService;
        this.relationshipService = relationshipService;
        this.filter = filter;
    }

    private Mono<Boolean> areAnyRelationsDormant(ServiceInstanceDetail sid, Integer daysSinceLastUpdate) {
        // see if service instance has any bound applications
        Flux<AppRelationship> relations = relationshipService.findByServiceInstanceId(sid.getServiceInstanceId());
        return
                relations
                // get application details for each bound app id
                .flatMap(relation -> appDetailService.findByAppId(relation.getAppId()))
                // check whether or not the app is dormant
                .flatMap(appDetail -> eventsService
                        .isDormantApplication(appDetail, daysSinceLastUpdate))
                .collectList()
                // result is a union; service instance deemed not dormant if any one of the applications is not dormant
                .map(list -> list.isEmpty() ? Boolean.FALSE : BooleanUtils.or(list.toArray(Boolean[]::new)));
    }

    public Mono<List<AppDetail>> getDormantApplications(HygienePolicy policy) {
        return snapshotService
                .assembleSnapshotDetail()
                .flatMapMany(sd -> Flux.fromIterable(sd.getApplications()))
                .filter(app -> filter.isWhitelisted(policy, app.getOrganization()))
                .filter(app -> filter.isBlacklisted(app.getOrganization(), app.getSpace()))
                .filter(app -> app.getRequestedState().equalsIgnoreCase("started"))
                // @see https://github.com/reactor/reactor-core/issues/498
                .filterWhen(app -> eventsService.isDormantApplication(app, policy.getDaysSinceLastUpdate()))
                .collectList();
    }

    public Mono<List<AppDetail>> getDormantApplications(Integer daysSinceLastUpdate) {
        return getDormantApplications(HygienePolicy.builder().daysSinceLastUpdate(daysSinceLastUpdate).build());
    }

    public Mono<List<ServiceInstanceDetail>> getDormantServiceInstances(HygienePolicy policy) {
        return snapshotService
                .assembleSnapshotDetail()
                .flatMapMany(sd -> Flux.fromIterable(sd.getServiceInstances()))
                .filter(sid -> filter.isWhitelisted(policy, sid.getOrganization()))
                .filter(sid -> filter.isBlacklisted(sid.getOrganization(), sid.getSpace()))
                // @see https://github.com/reactor/reactor-core/issues/498
                .filterWhen(sid -> eventsService.isDormantServiceInstance(sid, policy.getDaysSinceLastUpdate()))
                // we should also check that if service instance is bound to one or more apps,
                // then use the event date associated with each relation (i.e., bound application) to determine whether or not service instance is dormant
                .filterWhen(sid -> areAnyRelationsDormant(sid, policy.getDaysSinceLastUpdate()))
                .collectList();
    }

    public Mono<List<ServiceInstanceDetail>> getDormantServiceInstances(Integer daysSinceLastUpdate) {
        return getDormantServiceInstances(HygienePolicy.builder().daysSinceLastUpdate(daysSinceLastUpdate).build());
    }

}
