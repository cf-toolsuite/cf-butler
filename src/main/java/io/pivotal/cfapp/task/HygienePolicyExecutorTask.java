package io.pivotal.cfapp.task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.DormantWorkloads;
import io.pivotal.cfapp.domain.DormantWorkloads.DormantWorkloadsBuilder;
import io.pivotal.cfapp.domain.HygienePolicy;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.domain.UserSpaces;
import io.pivotal.cfapp.event.EmailNotificationEvent;
import io.pivotal.cfapp.service.DormantWorkloadsService;
import io.pivotal.cfapp.service.PoliciesService;
import io.pivotal.cfapp.service.SpaceUsersService;
import io.pivotal.cfapp.service.UserSpacesService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
@Component
public class HygienePolicyExecutorTask implements PolicyExecutorTask {

    private final PasSettings settings;
    private final PoliciesService policiesService;
    private final SpaceUsersService spaceUsersService;
    private final UserSpacesService userSpacesService;
    private final DormantWorkloadsService dormantWorkloadsService;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public HygienePolicyExecutorTask(
        PasSettings settings,
        PoliciesService policiesService,
        SpaceUsersService spaceUsersService,
        UserSpacesService userSpacesService,
        DormantWorkloadsService dormantWorkloadsService,
        ApplicationEventPublisher publisher
    ) {
        this.settings = settings;
        this.policiesService = policiesService;
        this.spaceUsersService = spaceUsersService;
        this.userSpacesService = userSpacesService;
        this.dormantWorkloadsService = dormantWorkloadsService;
        this.publisher = publisher;
    }

	@Override
    public void execute() {
	log.info("HygienePolicyExecutorTask started");
        fetchHygienePolicies()
            .concatMap(hp -> executeHygienePolicy(hp.getDaysSinceLastUpdate()).map(result -> Tuples.of(hp, result)))
            .collectList()
	    	.subscribe(
		    results -> {
		        results.forEach(tuple -> {
			    notifyOperator(tuple);
			    notifyUsers(tuple);
		    });
		    log.info("HygienePolicyExecutorTask completed");
		    log.info("-- {} hygiene policies executed.", results.size());
		},
		error -> {
		    log.error("HygienePolicyExecutorTask terminated with error", error);
		}
	);
    }

    private void notifyOperator(Tuple2<HygienePolicy, DormantWorkloads> tuple) {
        publisher.publishEvent(
            new EmailNotificationEvent(this)
                .domain(settings.getAppsDomain())
                .from(tuple.getT1().getOperatorTemplate().getFrom())
                .recipients(tuple.getT1().getOperatorTemplate().getTo())
                .subject(tuple.getT1().getOperatorTemplate().getSubject())
                .body(tuple.getT1().getOperatorTemplate().getBody())
                .attachmentContents(buildAttachmentContents(tuple.getT2()))
        );
    }

    private void notifyUsers(Tuple2<HygienePolicy, DormantWorkloads> tuple) {
        // pull distinct Set<Space> from dormant applications and service instances
        Flux<Space> spaces = Flux.fromIterable(getSpaces(tuple.getT2()));
        // for each Space in Set<Space>, obtain SpaceUsers#getUsers(),
        Flux<SpaceUsers> spaceUsers = spaces.flatMap(space -> spaceUsersService.findByOrganizationAndSpace(space.getOrganization(), space.getSpace()));
        // then pair with matching space(s) in dormant applications and service instances
        Flux<String> users = spaceUsers.flatMap(spaceUser -> Flux.fromIterable(spaceUser.getUsers())).distinct();
        Flux<UserSpaces> userSpaces = users.flatMap(user -> userSpacesService.getUserSpaces(user));
        // create a list where each item is a tuple of user account and filtered dormant workloads
        Flux<Tuple2<UserSpaces, DormantWorkloads>> filteredWorkloads = userSpaces.flatMap(userSpace -> filterDormantWorkloads(userSpace, tuple.getT2()));
        filteredWorkloads.map(workload -> {
                        publisher.publishEvent(
                            new EmailNotificationEvent(this)
                                .domain(settings.getAppsDomain())
                                .from(tuple.getT1().getNotifyeeTemplate().getFrom())
                                .recipients(Arrays.asList(new String[] { workload.getT1().getAccountName() }))
                                .subject(tuple.getT1().getNotifyeeTemplate().getSubject())
                                .body(tuple.getT1().getNotifyeeTemplate().getBody())
                                .attachmentContents(buildAttachmentContents(workload.getT2()))
                        );
                        return workload;
        })
        .subscribe();
    }

    private Mono<Tuple2<UserSpaces, DormantWorkloads>> filterDormantWorkloads(UserSpaces userSpaces, DormantWorkloads workloads){
        return Mono.just(Tuples.of(userSpaces, workloads.match(userSpaces.getSpaces())));

    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
    	execute();
    }

	protected Flux<HygienePolicy> fetchHygienePolicies() {
        return
            policiesService
		.findAllHygienePolicies()
                .flatMapMany(policy -> Flux.fromIterable(policy.getHygienePolicies()));
    }

    protected Mono<DormantWorkloads> executeHygienePolicy(Integer daysSinceLastUpdate) {
        final DormantWorkloadsBuilder builder = DormantWorkloads.builder();
        return dormantWorkloadsService
            .getDormantApplications(daysSinceLastUpdate)
            .map(list -> builder.applications(list))
            .then(dormantWorkloadsService.getDormantServiceInstances(daysSinceLastUpdate))
            .map(list -> builder.serviceInstances(list).build());
    }

    private static Map<String, String> buildAttachmentContents(DormantWorkloads workloads) {
	String cr = System.getProperty("line.separator");
        Map<String, String> result = new HashMap<>();
        StringBuilder dormantApplications = new StringBuilder();
        StringBuilder dormantServiceInstances = new StringBuilder();
        dormantApplications.append(AppDetail.headers()).append(cr);
        workloads
            .getApplications()
                .forEach(app -> dormantApplications.append(app.toCsv()).append(cr));
        dormantServiceInstances.append(ServiceInstanceDetail.headers()).append(cr);
        workloads
            .getServiceInstances()
                .forEach(sid -> dormantServiceInstances.append(sid.toCsv()).append(cr));
        result.put("dormant-applications", dormantApplications.toString());
        result.put("dormant-service-instances", dormantServiceInstances.toString());
        return result;
    }

    private static Set<Space> getSpaces(DormantWorkloads workloads) {
        Set<Space> applicationSpaces =
            workloads
                .getApplications()
                    .stream()
                        .map(app -> new Space(app.getOrganization(), app.getSpace()))
                        .collect(Collectors.toSet());
        Set<Space> serviceInstanceSpaces =
            workloads
                .getServiceInstances()
                    .stream()
                        .map(app -> new Space(app.getOrganization(), app.getSpace()))
                        .collect(Collectors.toSet());
        Set<Space> result = new HashSet<>();
        result.addAll(applicationSpaces);
        result.addAll(serviceInstanceSpaces);
        return result;
    }

}
