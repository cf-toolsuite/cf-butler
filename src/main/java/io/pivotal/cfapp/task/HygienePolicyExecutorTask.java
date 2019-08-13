package io.pivotal.cfapp.task;

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
import io.pivotal.cfapp.event.EmailNotificationEvent;
import io.pivotal.cfapp.service.DormantWorkloadsService;
import io.pivotal.cfapp.service.PoliciesService;
import io.pivotal.cfapp.service.SpaceUsersService;
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
    private final DormantWorkloadsService dormantWorkloadsService;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public HygienePolicyExecutorTask(
        PasSettings settings,
        PoliciesService policiesService,
        SpaceUsersService spaceUsersService,
        DormantWorkloadsService dormantWorkloadsService,
        ApplicationEventPublisher publisher
    ) {
        this.settings = settings;
        this.policiesService = policiesService;
        this.spaceUsersService = spaceUsersService;
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

    // FIXME Incomplete implementation
    private void notifyUsers(Tuple2<HygienePolicy, DormantWorkloads> tuple) {
        // pull distinct Set<Space> from dormant applications and service instances
        Flux<Space> spaces = Flux.fromIterable(getSpaces(tuple.getT2()));
        // for each Space in Set<Space>, obtain SpaceUsers#getUsers(),
        Flux<SpaceUsers> spaceUsers = spaces.flatMap(space -> spaceUsersService.findByOrganizationAndSpace(space.getOrganization(), space.getSpace()));
        // then pair with matching space(s) in dormant applications and service instances
        // create a list where each item is a tuple of user account and filtered dormant workloads
        publisher.publishEvent(
            new EmailNotificationEvent(this)
                .domain(settings.getAppsDomain())
                .from(tuple.getT1().getNotifyeeTemplate().getFrom())
                .recipients(tuple.getT1().getNotifyeeTemplate().getTo())
                .subject(tuple.getT1().getNotifyeeTemplate().getSubject())
                .body(tuple.getT1().getNotifyeeTemplate().getBody())
                .attachmentContents(buildAttachmentContents(tuple.getT2()))
        );
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
        Map<String, String> result = new HashMap<>();
        StringBuilder dormantApplications = new StringBuilder();
        StringBuilder dormantServiceInstances = new StringBuilder();
        dormantApplications.append(AppDetail.headers()).append(System.getProperty("line.separator"));
        workloads
            .getApplications()
                .forEach(app -> dormantApplications.append(app.toCsv()).append(System.getProperty("line.separator")));
        dormantServiceInstances.append(ServiceInstanceDetail.headers()).append(System.getProperty("line.separator"));
        workloads
            .getServiceInstances()
                .forEach(sid -> dormantServiceInstances.append(sid.toCsv()).append(System.getProperty("line.separator")));
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
