package io.pivotal.cfapp.task;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.EmailAttachment;
import io.pivotal.cfapp.domain.EmailValidator;
import io.pivotal.cfapp.domain.HygienePolicy;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.Space;
import io.pivotal.cfapp.domain.UserSpaces;
import io.pivotal.cfapp.domain.Workloads;
import io.pivotal.cfapp.domain.Workloads.WorkloadsBuilder;
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

    protected Mono<Workloads> executeHygienePolicy(HygienePolicy policy) {
        final WorkloadsBuilder builder = Workloads.builder();
        return dormantWorkloadsService
                .getDormantApplications(policy)
                .map(list -> builder.applications(list))
                .then(dormantWorkloadsService.getDormantServiceInstances(policy))
                .map(list -> builder.serviceInstances(list).build());
    }

    protected Flux<HygienePolicy> fetchHygienePolicies() {
        return
            policiesService
                .findAllHygienePolicies()
                .flatMapMany(policy -> Flux.fromIterable(policy.getHygienePolicies()));
    }

    private void notifyOperator(Tuple2<HygienePolicy, Workloads> tuple) {
        log.trace("User: admin, " + tuple.getT2().toString());
        publisher.publishEvent(
            new EmailNotificationEvent(this)
                .domain(settings.getAppsDomain())
                .from(tuple.getT1().getOperatorTemplate().getFrom())
                .recipients(tuple.getT1().getOperatorTemplate().getTo())
                .subject(tuple.getT1().getOperatorTemplate().getSubject())
                .body(tuple.getT1().getOperatorTemplate().getBody())
                .attachments(buildAttachments(tuple))
        );
    }

    private void notifyUsers(Tuple2<HygienePolicy, Workloads> tuple) {
        if (tuple.getT1().getNotifyeeTemplate() != null) {
            // Pull distinct Set<Space> from applications and service instances
            Flux
            .fromIterable(getSpaces(tuple.getT2()))
            // For each Space in Set<Space>, obtain SpaceUsers#getUsers()
            .concatMap(space -> spaceUsersService.findByOrganizationAndSpace(space.getOrganizationName(), space.getSpaceName()))
            // then pair with matching space(s) that contain applications and service instances
            .concatMap(spaceUser -> Flux.fromIterable(spaceUser.getUsers()))
            .distinct()
            // filter out account names that are not email addresses
            .filter(EmailValidator::isValid)
            .concatMap(userName -> userSpacesService.getUserSpaces(userName))
            // Create a list where each item is a tuple of user account and filtered workloads
            .concatMap(userSpace -> filterWorkloads(userSpace, tuple.getT2()))
            .delayElements(Duration.ofMillis(250))
            .doOnNext(
                userWorkloads -> {
                    publisher.publishEvent(
                        new EmailNotificationEvent(this)
                            .domain(settings.getAppsDomain())
                            .from(tuple.getT1().getNotifyeeTemplate().getFrom())
                            .recipient(userWorkloads.getT1().getAccountName())
                            .subject(tuple.getT1().getNotifyeeTemplate().getSubject())
                            .body(tuple.getT1().getNotifyeeTemplate().getBody())
                            .attachments(buildAttachments(Tuples.of(tuple.getT1(), userWorkloads.getT2())))
                    );
                }
            )
            .subscribe();
        }
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
        execute();
    }

    private static List<EmailAttachment> buildAttachments(Tuple2<HygienePolicy, Workloads> tuple) {
        String cr = System.getProperty("line.separator");
        List<EmailAttachment> result = new ArrayList<>();
        StringBuilder applications = new StringBuilder();
        StringBuilder serviceInstances = new StringBuilder();
        if (tuple.getT1().isIncludeApplications()) {
            tuple
                .getT2()
                .getApplications()
                .forEach(app -> applications.append(app.toCsv()).append(cr));
            result.add(
                EmailAttachment
                    .builder()
                    .filename(getFileNamePrefix(tuple.getT1()) + "applications")
                    .extension(".csv")
                    .mimeType("text/plain;charset=UTF-8")
                    .content(applications.toString())
                    .headers(AppDetail.headers())
                    .build()
            );
        }
        if (tuple.getT1().isIncludeServiceInstances()) {
            tuple
                .getT2()
                .getServiceInstances()
                .forEach(sid -> serviceInstances.append(sid.toCsv()).append(cr));
            result.add(
                EmailAttachment
                    .builder()
                    .filename(getFileNamePrefix(tuple.getT1()) + "service-instances")
                    .extension(".csv")
                    .mimeType("text/plain;charset=UTF-8")
                    .content(serviceInstances.toString())
                    .headers(ServiceInstanceDetail.headers())
                    .build()
            );
        }
        return result;
    }

    private static Space buildSpace(String organization, String space) {
        return Space
                .builder()
                .organizationName(organization)
                .spaceName(space)
                .build();
    }

    private static Mono<Tuple2<UserSpaces, Workloads>> filterWorkloads(UserSpaces userSpaces, Workloads input){
        Workloads workloads = input.matchBySpace(userSpaces.getSpaces());
        log.trace(userSpaces.toString() + ", " + workloads.toString());
        return Mono.just(Tuples.of(userSpaces, workloads));

    }

    private static String getFileNamePrefix(HygienePolicy policy) {
        String prefix = "";
        if (policy.getDaysSinceLastUpdate() != -1) {
            prefix = "dormant-";
        }
        return prefix;
    }

    private static Set<Space> getSpaces(Workloads workloads) {
        Set<Space> applicationSpaces =
            workloads
                .getApplications()
                .stream()
                .map(app -> buildSpace(app.getOrganization(), app.getSpace()))
                .collect(Collectors.toSet());
        Set<Space> serviceInstanceSpaces =
            workloads
                .getServiceInstances()
                .stream()
                .map(app -> buildSpace(app.getOrganization(), app.getSpace()))
                .collect(Collectors.toSet());
        Set<Space> result = new HashSet<>();
        result.addAll(applicationSpaces);
        result.addAll(serviceInstanceSpaces);
        return result;
    }

}
