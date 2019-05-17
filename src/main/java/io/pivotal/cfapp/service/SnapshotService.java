package io.pivotal.cfapp.service;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.config.ButlerSettings;
import io.pivotal.cfapp.domain.ApplicationCounts;
import io.pivotal.cfapp.domain.ServiceInstanceCounts;
import io.pivotal.cfapp.domain.SnapshotDetail;
import io.pivotal.cfapp.domain.SnapshotSummary;
import io.pivotal.cfapp.domain.UserCounts;
import io.pivotal.cfapp.report.AppDetailCsvReport;
import io.pivotal.cfapp.report.ServiceInstanceDetailCsvReport;
import io.pivotal.cfapp.report.UserAccountsCsvReport;
import io.pivotal.cfapp.task.AppDetailRetrievedEvent;
import io.pivotal.cfapp.task.ServiceInstanceDetailRetrievedEvent;
import io.pivotal.cfapp.task.UserAccountsRetrievedEvent;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
public class SnapshotService {

    private final AppDetailService appDetailService;
    private final ServiceInstanceDetailService siDetailService;
    private final AppRelationshipService appRelationshipService;
    private final SpaceUsersService spaceUsersService;
    private final AppMetricsService appMetricsService;
    private final ServiceInstanceMetricsService siMetricsService;
    private final AppDetailCsvReport appDetailCsvReport;
    private final ServiceInstanceDetailCsvReport siDetailCsvReport;
    private final UserAccountsCsvReport uaCsvReport;

    @Autowired
    public SnapshotService(
        ButlerSettings settings,
        AppDetailService appDetailService,
        ServiceInstanceDetailService siDetailService,
        AppRelationshipService appRelationshipService,
        SpaceUsersService spaceUsersService,
        AppMetricsService appMetricsService,
        ServiceInstanceMetricsService siMetricsService
        ) {
        this.appDetailService = appDetailService;
        this.siDetailService = siDetailService;
        this.appRelationshipService = appRelationshipService;
        this.spaceUsersService = spaceUsersService;
        this.appMetricsService = appMetricsService;
        this.siMetricsService = siMetricsService;
        this.appDetailCsvReport = new AppDetailCsvReport(settings);
        this.siDetailCsvReport = new ServiceInstanceDetailCsvReport(settings);
        this.uaCsvReport = new UserAccountsCsvReport(settings);
    }

    public Mono<String> assembleCsvAIReport() {
        return appDetailService
				.findAll()
				.collectList()
		        .map(r -> new AppDetailRetrievedEvent(this)
							.detail(r)
				)
		        .map(event ->
		        	String.join(
		        			"\n\n",
		        			appDetailCsvReport.generatePreamble(),
		        			appDetailCsvReport.generateDetail(event)));
    }

    public Mono<String> assembleCsvUserAccountReport() {
        return spaceUsersService
				.obtainUserAccounts()
				.collectList()
		        .map(r -> new UserAccountsRetrievedEvent(this)
							.detail(r)
				)
		        .map(event ->
		        	String.join(
		        			"\n\n",
		        			uaCsvReport.generatePreamble(),
		        			uaCsvReport.generateDetail(event)));
    }

    public Mono<String> assembleCsvSIReport() {
        return siDetailService
				.findAll()
				.collectList()
		        .map(r -> new ServiceInstanceDetailRetrievedEvent(this)
							.detail(r)
				)
		        .map(event ->
		        	String.join(
		        			"\n\n",
		        			siDetailCsvReport.generatePreamble(),
		        			siDetailCsvReport.generateDetail(event)));
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
                                        .obtainAccountNames()
                                            .collect(Collectors.toSet())
                                            .map(u -> b.accounts(u).build()));
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
                    .flatMap(b -> spaceUsersService.totalUserAccounts().map(c -> b.totalUserAccounts(c)))
                    .flatMap(b -> spaceUsersService.totalServiceAccounts().map(c -> b.totalServiceAccounts(c).build()));
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
                .flatMap(b -> appMetricsService.totalCrashedApplicationInstances().map(taai -> b.totalCrashedApplicationInstances(taai)))
                .flatMap(b -> appMetricsService.totalApplicationInstances().map(tai -> b.totalApplicationInstances(tai)))
                .flatMap(b -> appMetricsService.totalMemoryUsed().map(tmu -> b.totalMemoryUsed(tmu)))
                .flatMap(b -> appMetricsService.totalDiskUsed().map(tdu -> b.totalDiskUsed(tdu)))
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
