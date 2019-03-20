package io.pivotal.cfapp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppRelationship;
import io.pivotal.cfapp.domain.ApplicationCounts;
import io.pivotal.cfapp.domain.ServiceInstanceCounts;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.SnapshotDetail;
import io.pivotal.cfapp.domain.SnapshotSummary;
import io.pivotal.cfapp.domain.SnapshotSummary.SnapshotSummaryBuilder;
import io.pivotal.cfapp.domain.UserCounts;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

@Service
public class SnapshotService {

    private final AppDetailService appDetailService;
    private final ServiceInstanceDetailService siDetailService;
    private final AppRelationshipService appRelationshipService;
    private final SpaceUsersService spaceUsersService;
    private final AppMetricsService appMetricsService;
    private final ServiceInstanceMetricsService siMetricsService;

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
        final List<AppDetail> applications = new ArrayList<>();
        final List<ServiceInstanceDetail> serviceInstances = new ArrayList<>();
        final List<AppRelationship> applicationRelationships = new ArrayList<>();
        final Set<String> users = new HashSet<>();
        return appDetailService
                .findAll()
                    .map(ad -> applications.add(ad))
                    .thenMany(siDetailService
                                .findAll()
                                    .map(sid -> serviceInstances.add(sid))
                    )
                    .thenMany(appRelationshipService
                                .findAll()
                                    .map(ar -> applicationRelationships.add(ar))
                    )
                    .thenMany(spaceUsersService
                                .findAll()
                                    .map(su -> users.addAll(su.getUsers())))
                    .then(Mono.just(SnapshotDetail.builder()
                                            .applications(applications)
                                            .serviceInstances(serviceInstances)
                                            .applicationRelationships(applicationRelationships)
                                            .users(users)
                                            .build()));
        }

        public Mono<SnapshotSummary> assembleSnapshotSummary() {
            final SnapshotSummaryBuilder builder = SnapshotSummary.builder();
            return assembleApplicationCounts().map(c -> builder.applicationCounts(c))
                    .then(assembleServiceInstanceCounts().map(c -> builder.serviceInstanceCounts(c)))
                    .then(assembleUserCounts().map(c -> builder.userCounts(c)))
                    .then(Mono.just(builder.build()));
        }

        private Mono<UserCounts> assembleUserCounts() {
            final Map<String, Map<String, Integer>> r1 = new HashMap<>();
            final Map<String, Integer> r2 = new HashMap<>();
            return
                spaceUsersService.count().map(r -> r2.put("total-users", r))
                    .then(spaceUsersService.countByOrganization().map(r -> r1.put("by-organization", r)))
                    .then(Mono.just(UserCounts
                                        .builder()
                                            .byOrganization(r1.get("by-organization"))
                                            .totalUsers(r2.get("total-users"))
                                            .build()));
        }

        private Mono<ApplicationCounts> assembleApplicationCounts() {
            final Map<String,Long> byOrganization = new HashMap<>();
            final Map<String,Long> byBuildpack = new HashMap<>();
            final Map<String,Long> byStack = new HashMap<>();
            final Map<String,Long> byDockerImage = new HashMap<>();
            final Map<String,Long> byStatus = new HashMap<>();
            final Map<String,Long> velocity = new HashMap<>();
            final Map<String, Long> stats = new HashMap<>();
            return
                appMetricsService.byOrganization().map(r -> byOrganization.put(r.getT1(), r.getT2()))
                    .thenMany(appMetricsService.byBuildpack().map(r -> byBuildpack.put(r.getT1(), r.getT2())))
                    .thenMany(appMetricsService.byStack().map(r -> byStack.put(r.getT1(), r.getT2())))
                    .thenMany(appMetricsService.byDockerImage().map(r -> byDockerImage.put(r.getT1(), r.getT2())))
                    .thenMany(appMetricsService.byStatus().map(r -> byStatus.put(r.getT1(), r.getT2())))
                    .then(appMetricsService.totalApplications().map(r -> stats.put("total-applications", r)))
                    .then(appMetricsService.totalRunningApplicationInstances().map(r -> stats.put("total-running-application-instances", r)))
                    .then(appMetricsService.totalStoppedApplicationInstances().map(r -> stats.put("total-stopped-application-instances", r)))
                    .then(appMetricsService.totalApplicationInstances().map(r -> stats.put("total-application-instances", r)))
                    .thenMany(appMetricsService.totalVelocity().map(r -> velocity.put(r.getT1(), r.getT2())))
                    .then(Mono.just(ApplicationCounts
                                        .builder()
                                            .byOrganization(byOrganization)
                                            .byBuildpack(byBuildpack)
                                            .byDockerImage(byDockerImage)
                                            .byStatus(byStatus)
                                            .totalApplications(stats.get("total-applications"))
                                            .totalRunningApplicationInstances(stats.get("total-running-application-instances"))
                                            .totalStoppedApplicationInstances(stats.get("total-stopped-application-instances"))
                                            .totalApplicationInstances(stats.get("total-application-instances"))
                                            .velocity(velocity)
                                            .build()));
        }

        private Mono<ServiceInstanceCounts> assembleServiceInstanceCounts() {
            final Map<String,Long> byOrganization = new HashMap<>();
            final Map<String,Long> byService = new HashMap<>();
            final List<Tuple3<String, String, Long>> byServiceAndPlan = new ArrayList<>();
            final Map<String,Long> velocity = new HashMap<>();
            Map<String, Long> stats = new HashMap<>();
            return
                siMetricsService.byOrganization().map(r -> byOrganization.put(r.getT1(), r.getT2()))
                    .thenMany(siMetricsService.byService().map(r -> byService.put(r.getT1(), r.getT2())))
                    .thenMany(siMetricsService.byServiceAndPlan().map(r -> byServiceAndPlan.add(r)))
                    .then(siMetricsService.totalServiceInstances().map(r -> stats.put("total-service-instances", r)))
                    .thenMany(siMetricsService.totalVelocity().map(r -> velocity.put(r.getT1(), r.getT2())))
                    .then(Mono.just(ServiceInstanceCounts
                                        .builder()
                                            .byOrganization(byOrganization)
                                            .byService(byService)
                                            .byServiceAndPlan(byServiceAndPlan)
                                            .totalServiceInstances(stats.get("total-service-instances"))
                                            .velocity(velocity)
                                            .build()));
        }
    }