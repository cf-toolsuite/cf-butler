package io.pivotal.cfapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.AppDetail;
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
		    return snapshotService
                .assembleSnapshotDetail()
                .flatMapMany(sd -> Flux.fromIterable(sd.getApplications()))
                .filter(app -> isBlacklisted(app.getOrganization()))
                // @see https://github.com/reactor/reactor-core/issues/498
                .filterWhen(app -> eventsService.isDormantApplication(app.getAppId(), daysSinceLastUpdate))
                .collectList();
    }

    public Mono<List<ServiceInstanceDetail>> getDormantServiceInstances(Integer daysSinceLastUpdate) {
		    return snapshotService
                .assembleSnapshotDetail()
                .flatMapMany(sd -> Flux.fromIterable(sd.getServiceInstances()))
                .filter(sid -> isBlacklisted(sid.getOrganization()))
                // @see https://github.com/reactor/reactor-core/issues/498
                .filterWhen(sid -> eventsService.isDormantServiceInstance(sid.getServiceInstanceId(), daysSinceLastUpdate))
                .collectList();
    }

    private boolean isBlacklisted(String organization) {
        return !settings.getOrganizationBlackList().contains(organization);
    }
}