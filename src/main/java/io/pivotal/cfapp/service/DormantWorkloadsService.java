package io.pivotal.cfapp.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppRelationship;
import io.pivotal.cfapp.domain.HygienePolicy;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DormantWorkloadsService {

    private final EventsService eventsService;
    private final SnapshotService snapshotService;
    private final AppDetailService appDetailService;
    private final AppRelationshipService relationshipService;
    private final PasSettings settings;

    @Autowired
    public DormantWorkloadsService(
            EventsService eventsService,
            SnapshotService snapshotService,
            AppDetailService appDetailService,
            AppRelationshipService relationshipService,
            PasSettings settings
            ) {
        this.eventsService = eventsService;
        this.snapshotService = snapshotService;
        this.appDetailService = appDetailService;
        this.relationshipService = relationshipService;
        this.settings = settings;
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
                .filter(app -> isWhitelisted(policy, app.getOrganization()))
                .filter(app -> isBlacklisted(app.getOrganization()))
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
                .filter(sid -> isWhitelisted(policy, sid.getOrganization()))
                .filter(sid -> isBlacklisted(sid.getOrganization()))
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
