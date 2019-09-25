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
import io.pivotal.cfapp.domain.EmailValidator;
import io.pivotal.cfapp.domain.HygienePolicy;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.Space;
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
            .concatMap(hp -> executeHygienePolicy(hp).map(result -> Tuples.of(hp, result)))
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
        log.trace("User: admin, " + tuple.getT2().toString());
        publisher.publishEvent(
            new EmailNotificationEvent(this)
                .domain(settings.getAppsDomain())
                .from(tuple.getT1().getOperatorTemplate().getFrom())
                .recipients(tuple.getT1().getOperatorTemplate().getTo())
                .subject(tuple.getT1().getOperatorTemplate().getSubject())
                .body(tuple.getT1().getOperatorTemplate().getBody())
                .attachmentContents(buildAttachmentContents(tuple))
        );
    }

    private void notifyUsers(Tuple2<HygienePolicy, DormantWorkloads> tuple) {
        // Pull distinct Set<Space> from dormant applications and service instances
        Flux
            .fromIterable(getSpaces(tuple.getT2()))
        // For each Space in Set<Space>, obtain SpaceUsers#getUsers()
            .concatMap(space -> spaceUsersService.findByOrganizationAndSpace(space.getOrganization(), space.getSpace()))
        // then pair with matching space(s) in dormant applications and service instances
            .concatMap(spaceUser -> Flux.fromIterable(spaceUser.getUsers()))
            .distinct()
        // filter out account names that are not email addresses
            .filter(userName -> EmailValidator.isValid(userName))
            .concatMap(userName -> userSpacesService.getUserSpaces(userName))
        // Create a list where each item is a tuple of user account and filtered dormant workloads
            .concatMap(userSpace -> filterDormantWorkloads(userSpace, tuple.getT2()))
            .map(workload -> {
                    publisher.publishEvent(
                        new EmailNotificationEvent(this)
                            .domain(settings.getAppsDomain())
                            .from(tuple.getT1().getNotifyeeTemplate().getFrom())
                            .recipient(workload.getT1().getAccountName())
                            .subject(tuple.getT1().getNotifyeeTemplate().getSubject())
                            .body(tuple.getT1().getNotifyeeTemplate().getBody())
                            .attachmentContents(buildAttachmentContents(Tuples.of(tuple.getT1(), workload))
                    );
                    return workload;
            })
            .subscribe();
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

    protected Mono<DormantWorkloads> executeHygienePolicy(HygienePolicy policy) {
        final DormantWorkloadsBuilder builder = DormantWorkloads.builder();
        return dormantWorkloadsService
            .getDormantApplications(policy)
            .map(list -> builder.applications(list))
            .then(dormantWorkloadsService.getDormantServiceInstances(policy))
            .map(list -> builder.serviceInstances(list).build());
    }

    private static Mono<Tuple2<UserSpaces, DormantWorkloads>> filterDormantWorkloads(UserSpaces userSpaces, DormantWorkloads workloads){
        DormantWorkloads dormantWorkloads = workloads.matchBySpace(userSpaces.getSpaces());
        log.trace(userSpaces.toString() + ", " + dormantWorkloads.toString());
        return Mono.just(Tuples.of(userSpaces, dormantWorkloads));

    }

    private static Map<String, String> buildAttachmentContents(Tuple2<HygienePolicy, DormantWorkloads> tuple) {
	    String cr = System.getProperty("line.separator");
        Map<String, String> result = new HashMap<>();
        StringBuilder applications = new StringBuilder();
        StringBuilder serviceInstances = new StringBuilder();
        applications.append(AppDetail.headers()).append(cr);
        tuple.getT2()
            .getApplications()
                .forEach(app -> applications.append(app.toCsv()).append(cr));
        serviceInstances.append(ServiceInstanceDetail.headers()).append(cr);
        tuple.getT2()
            .getServiceInstances()
                .forEach(sid -> serviceInstances.append(sid.toCsv()).append(cr));
        result.put(getFileNamePrefix(tuple.getT1()) + "applications", applications.toString());
        result.put(getFileNamePrefix(tuple.getT1()) + "service-instances", serviceInstances.toString());
        return result;
    }

    private static String getFileNamePrefix(HygienePolicy policy) {
        String prefix = "";
        if (policy.getDaysSinceLastUpdate() != -1) {
            prefix = "dormant-";
        }
        return prefix;
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
