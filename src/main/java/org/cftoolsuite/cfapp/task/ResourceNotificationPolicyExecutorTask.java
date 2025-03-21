package org.cftoolsuite.cfapp.task;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.domain.ResourceNotificationPolicy;
import org.cftoolsuite.cfapp.event.EmailNotificationEvent;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.cftoolsuite.cfapp.service.ResourceMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ResourceNotificationPolicyExecutorTask implements PolicyExecutorTask {

    private final PasSettings settings;

    private final PoliciesService policiesService;

    private final ApplicationEventPublisher publisher;

    private final ResourceMetadataService resourceMetadataService;


    @Autowired
    public ResourceNotificationPolicyExecutorTask(
        PasSettings settings,
        PoliciesService policiesService,
        ApplicationEventPublisher publisher,
        ResourceMetadataService resourceMetadataService) {
                this.settings = settings;
                this.policiesService = policiesService;
                this.publisher = publisher;
                this.resourceMetadataService = resourceMetadataService;
        }

    @Override
    public void execute(String id) {
        log.info("ResourceNotificationPolicyExecutorTask with id={} started", id);
        fetchResourceNotificationPolicy(id)
            .collectList()
            .subscribe(
                results -> {
                    results.forEach(mp -> {
                        mp.getResourceEmailMetadata().getLabels().forEach(label -> {
                            notifyOwners(mp,label);
                        });
                    });
                    log.info("ResourceNotificationPolicyExecutorTask with id={} completed", id);
                },
                error -> {
                    log.error(String.format("ResourceNotificationPolicyExecutorTask with id=%s terminated with error", id), error);
                }
            );
    }

    protected Flux<ResourceNotificationPolicy> fetchResourceNotificationPolicy(String id) {
        return
            policiesService
                .findResourceNotificationPolicyById(id)
                .flatMapMany(policy -> Flux.fromIterable(policy.getResourceNotificationPolicies()));
    }

    private Mono<Set<String>> fetchRecipientList(ResourceNotificationPolicy resourceNotificationPolicy, String label, Integer page, Integer perPage){
        return
            resourceMetadataService.getResources(resourceNotificationPolicy.getResourceEmailMetadata().getResource(), label, page, perPage)
                .flatMapMany(resources -> Flux.fromIterable(resources.getResources()))
                .delayElements(Duration.ofMillis(250))
                .filter(resource -> isBlacklisted(resourceNotificationPolicy, resource.getName()))
                .filter(resource -> isWhitelisted(resourceNotificationPolicy, resource.getName()))
                .map(resource -> new String(resource.getMetadata().getLabels().get(label) + "@" + resourceNotificationPolicy.getResourceEmailMetadata().getEmailDomain()))
                .collect(Collectors.toSet());
    }

    private void notifyOwners(ResourceNotificationPolicy resourceNotificationPolicy, String label) {
        resourceMetadataService.getResources(resourceNotificationPolicy.getResourceEmailMetadata().getResource(), label , null , null)
            .delayElement(Duration.ofMillis(250))
            .doOnNext(resources -> {
                for (Integer page=1; page <= resources.getPagination().getTotalPages(); page++) {
                    fetchRecipientList(resourceNotificationPolicy, label, page, null)
                        .doOnNext(
                                recipients -> {
                                    publisher.publishEvent(
                                        new EmailNotificationEvent(this)
                                            .domain(settings.getAppsDomain())
                                            .from(resourceNotificationPolicy.getResourceEmailTemplate().getFrom())
                                            .recipients(recipients)
                                            .subject(resourceNotificationPolicy.getResourceEmailTemplate().getSubject())
                                            .body(resourceNotificationPolicy.getResourceEmailTemplate().getBody()));
                                })
                        .subscribe();
                    }
                })
            .subscribe();
    }

    private boolean isBlacklisted(ResourceNotificationPolicy policy, String resource) {
        return !policy.getResourceBlackList().contains(resource);
    }


    private boolean isWhitelisted(ResourceNotificationPolicy policy, String resource) {
        Set<String> prunedSet = new HashSet<>(policy.getResourceWhiteList());
        while (prunedSet.remove(""));
        Set<String> whitelist =
                CollectionUtils.isEmpty(prunedSet) ?
                        prunedSet: policy.getResourceWhiteList();
        return
                whitelist.isEmpty() || policy.getResourceWhiteList().contains(resource);
    }

}