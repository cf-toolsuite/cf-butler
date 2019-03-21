package io.pivotal.cfapp.service;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.ApplicationCounts;
import io.pivotal.cfapp.domain.ServiceInstanceCounts;
import io.pivotal.cfapp.domain.SnapshotDetail;
import io.pivotal.cfapp.domain.SnapshotSummary;
import io.pivotal.cfapp.domain.UserCounts;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
public class SnapshotService {

    protected final AppDetailService appDetailService;
    protected final ServiceInstanceDetailService siDetailService;
    protected final AppRelationshipService appRelationshipService;
    protected final SpaceUsersService spaceUsersService;
    protected final AppMetricsService appMetricsService;
    protected final ServiceInstanceMetricsService siMetricsService;

    @Autowired
    public SnapshotService(
        AppDetailService appDetailService,
        ServiceInstanceDetailService siDetailService,
        AppRelationshipService appRelationshipService,
        SpaceUsersService spaceUsersService,
        AppMetricsService appMetricsService,
        ServiceInstanceMetricsService siMetricsService) {
        this.appDetailService = appDetailService;
        this.siDetailService = siDetailService;
        this.appRelationshipService = appRelationshipService;
        this.spaceUsersService = spaceUsersService;
        this.appMetricsService = appMetricsService;
        this.siMetricsService = siMetricsService;
    }

    public Mono<SnapshotDetail> assembleSnapshotDetail() {
        return appDetailService
                .findAll()
                    .collectList()
                        .map(ad -> SnapshotDetail.builder().applications(ad))
                        .flatMap(b -> siDetailService
                                        .findAll()
                                            .collectList()
                                                .map(sid -> b.serviceInstances(sid)))
                        .flatMap(b -> appRelationshipService
                                        .findAll()
                                            .collectList()
                                                .map(ar -> b.applicationRelationships(ar)))
                        .flatMap(b -> spaceUsersService
                                        .obtainUniqueUsernames()
                                            .map(u -> b.users(u).build()));
        }

        public Mono<SnapshotSummary> assembleSnapshotSummary() {
            return assembleApplicationCounts()
                    .map(ac -> SnapshotSummary.builder().applicationCounts(ac))
                    .flatMap(b -> assembleServiceInstanceCounts().map(sic -> b.serviceInstanceCounts(sic)))
                    .flatMap(b -> assembleUserCounts().map(uc -> b.userCounts(uc).build()));
        }

        protected Mono<UserCounts> assembleUserCounts() {
            return spaceUsersService
                    .countByOrganization()
                        .map(cbo -> UserCounts.builder().byOrganization(cbo))
                        .flatMap(b -> spaceUsersService.count().map(c -> b.totalUsers(c).build()));
        }

        protected Mono<ApplicationCounts> assembleApplicationCounts() {
            return
                appMetricsService.byOrganization().collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)).map(bo -> ApplicationCounts.builder().byOrganization(bo))
                    .flatMap(b -> appMetricsService.byBuildpack().collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)).map(bb -> b.byBuildpack(bb)))
                    .flatMap(b -> appMetricsService.byStack().collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)).map(bs -> b.byStack(bs)))
                    .flatMap(b -> appMetricsService.byDockerImage().collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)).map(bdi -> b.byDockerImage(bdi)))
                    .flatMap(b -> appMetricsService.byStatus().collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)).map(bst -> b.byStatus(bst)))
                    .flatMap(b -> appMetricsService.totalApplications().map(ta -> b.totalApplications(ta)))
                    .flatMap(b -> appMetricsService.totalRunningApplicationInstances().map(trai -> b.totalRunningApplicationInstances(trai)))
                    .flatMap(b -> appMetricsService.totalStoppedApplicationInstances().map(tsai -> b.totalStoppedApplicationInstances(tsai)))
                    .flatMap(b -> appMetricsService.totalAnomalousApplicationInstances().map(taai -> b.totalAnomalousApplicationInstances(taai)))
                    .flatMap(b -> appMetricsService.totalApplicationInstances().map(tai -> b.totalApplicationInstances(tai)))
                    .flatMap(b -> appMetricsService.totalApplicationInstances().map(tai -> b.totalApplicationInstances(tai)))
                    .flatMap(b -> appMetricsService.totalVelocity().collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)).map(v -> b.velocity(v).build()));
        }

        protected Mono<ServiceInstanceCounts> assembleServiceInstanceCounts() {
            return
                siMetricsService.byOrganization().collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)).map(bo -> ServiceInstanceCounts.builder().byOrganization(bo))
                    .flatMap(b -> siMetricsService.byService().collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)).map(bs -> b.byService(bs)))
                    .flatMap(b -> siMetricsService.byServiceAndPlan().collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)).map(bsap -> b.byServiceAndPlan(bsap)))
                    .flatMap(b -> siMetricsService.totalServiceInstances().map(tsi -> b.totalServiceInstances(tsi)))
                    .flatMap(b -> siMetricsService.totalVelocity().collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)).map(v -> b.velocity(v).build()));
        }
    }