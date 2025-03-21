package org.cftoolsuite.cfapp.task;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.domain.AppRelationship;
import org.cftoolsuite.cfapp.domain.EmailAttachment;
import org.cftoolsuite.cfapp.domain.EmailValidator;
import org.cftoolsuite.cfapp.domain.LegacyPolicy;
import org.cftoolsuite.cfapp.domain.Space;
import org.cftoolsuite.cfapp.domain.UserSpaces;
import org.cftoolsuite.cfapp.domain.Workloads;
import org.cftoolsuite.cfapp.domain.Workloads.WorkloadsBuilder;
import org.cftoolsuite.cfapp.event.EmailNotificationEvent;
import org.cftoolsuite.cfapp.service.LegacyWorkloadsService;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.service.SpaceUsersService;
import org.cftoolsuite.cfapp.service.UserSpacesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
@Component
public class LegacyWorkloadReportingTask implements PolicyExecutorTask {

    private final PasSettings settings;
    private final PoliciesService policiesService;
    private final SpaceUsersService spaceUsersService;
    private final UserSpacesService userSpacesService;
    private final LegacyWorkloadsService legacyWorkloadsService;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public LegacyWorkloadReportingTask(
            PasSettings settings,
            PoliciesService policiesService,
            SpaceUsersService spaceUsersService,
            UserSpacesService userSpacesService,
            LegacyWorkloadsService legacyWorkloadsService,
            ApplicationEventPublisher publisher
            ) {
        this.settings = settings;
        this.policiesService = policiesService;
        this.spaceUsersService = spaceUsersService;
        this.userSpacesService = userSpacesService;
        this.legacyWorkloadsService = legacyWorkloadsService;
        this.publisher = publisher;
    }

    @Override
    public void execute(String id) {
        log.info("LegacyWorkloadReportingTask with id={} started", id);
        fetchLegacyPolicy(id)
        .concatMap(hp -> executePolicy(hp).map(result -> Tuples.of(hp, result)))
        .collectList()
        .subscribe(
            results -> {
                results.forEach(tuple -> {
                    notifyOperator(tuple);
                    notifyUsers(tuple);
                });
                log.info("LegacyWorkloadReportingTask with id={} completed", id);
            },
            error -> {
                log.error(String.format("LegacyWorkloadReportingTask with id=%s terminated with error", id), error);
            }
        );
    }

    protected Mono<Workloads> executePolicy(LegacyPolicy policy) {
        final WorkloadsBuilder builder = Workloads.builder();
        return legacyWorkloadsService
                .getLegacyApplications(policy)
                .map(builder::applications)
                .then(legacyWorkloadsService.getLegacyApplicationRelationships(policy))
                .map(list -> builder.appRelationships(list).build());
    }

    protected Flux<LegacyPolicy> fetchLegacyPolicy(String id) {
        return
            policiesService
                .findLegacyPolicyById(id)
                .flatMapMany(policy -> Flux.fromIterable(policy.getLegacyPolicies()));
    }

    private void notifyOperator(Tuple2<LegacyPolicy, Workloads> tuple) {
        log.trace("User: admin, " + tuple.getT2().toString());
        publisher.publishEvent(
            new EmailNotificationEvent(this)
                .domain(settings.getAppsDomain())
                .from(tuple.getT1().getOperatorTemplate().getFrom())
                .recipients(tuple.getT1().getOperatorTemplate().getTo())
                .carbonCopyRecipients(tuple.getT1().getOperatorTemplate().getCc())
                .blindCarbonCopyRecipients(tuple.getT1().getOperatorTemplate().getBcc())
                .subject(tuple.getT1().getOperatorTemplate().getSubject())
                .body(tuple.getT1().getOperatorTemplate().getBody())
                .attachments(buildAttachments(tuple))
        );
    }

    private void notifyUsers(Tuple2<LegacyPolicy, Workloads> tuple) {
        if (tuple.getT1().getNotifyeeTemplate() != null) {
            // Pull distinct Set<Space> from applications and service instances
            Flux
            .fromIterable(getSpaces(tuple.getT2()))
            // For each Space in Set<Space>, obtain SpaceUsers#getUsers()
            .concatMap(space -> spaceUsersService.findByOrganizationAndSpace(space.getOrganizationName(), space.getSpaceName()))
            // then pair with matching space(s) that contain applications and service instances
            .concatMap(spaceUser -> Flux.fromIterable(spaceUser.getDevelopers()))
            .distinct()
            // filter out account names that are not email addresses
            .filter(EmailValidator::isValid)
            .concatMap(userSpacesService::getUserSpaces)
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

    private static List<EmailAttachment> buildAttachments(Tuple2<LegacyPolicy, Workloads> tuple) {
        String cr = System.lineSeparator();
        List<EmailAttachment> result = new ArrayList<>();
        StringBuilder content = new StringBuilder();
        if (!tuple.getT2().getApplications().isEmpty()){
            tuple
                .getT2()
                .getApplications()
                .forEach(app -> content.append(app.toCsv()).append(cr));
            result.add(
                EmailAttachment
                    .builder()
                    .filename(getFileNamePrefix(tuple.getT1()) + "applications")
                    .extension(".csv")
                    .mimeType("text/plain;charset=UTF-8")
                    .content(content.toString())
                    .headers(AppDetail.headers())
                    .build()
            );
        }
        if (!tuple.getT2().getAppRelationships().isEmpty()){
            tuple
                .getT2()
                .getAppRelationships()
                .forEach(app -> content.append(app.toCsv()).append(cr));
            result.add(
                EmailAttachment
                    .builder()
                    .filename(getFileNamePrefix(tuple.getT1()) + "applications")
                    .extension(".csv")
                    .mimeType("text/plain;charset=UTF-8")
                    .content(content.toString())
                    .headers(AppRelationship.headers())
                    .build()
            );
        }
        return result;
    }

    private static Mono<Tuple2<UserSpaces, Workloads>> filterWorkloads(UserSpaces userSpaces, Workloads input){
        Workloads workloads = input.matchBySpace(userSpaces.getSpaces());
        log.trace(userSpaces.toString() + ", " + workloads.toString());
        return Mono.just(Tuples.of(userSpaces, workloads));

    }

    private static String getFileNamePrefix(LegacyPolicy policy) {
        return "legacy-";
    }

    private static Set<Space> getSpaces(Workloads workloads) {
        Set<Space> applicationSpaces =
            workloads
                .getApplications()
                .stream()
                .map(app ->
                    Space
                        .builder()
                        .organizationName(app.getOrganization())
                        .spaceName(app.getSpace())
                        .build()
                )
                .collect(Collectors.toSet());
        Set<Space> appRelationshipsSpaces =
            workloads
                .getAppRelationships()
                .stream()
                .map(rel ->
                    Space
                        .builder()
                        .organizationName(rel.getOrganization())
                        .spaceName(rel.getSpace())
                        .build())
                .collect(Collectors.toSet());
        Set<Space> result = new HashSet<>();
        result.addAll(applicationSpaces);
        result.addAll(appRelationshipsSpaces);
        return result;
    }
}
