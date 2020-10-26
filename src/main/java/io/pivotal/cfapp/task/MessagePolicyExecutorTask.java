package io.pivotal.cfapp.task;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.MessagePolicy;
import io.pivotal.cfapp.event.EmailNotificationEvent;
import io.pivotal.cfapp.service.PoliciesService;
import io.pivotal.cfapp.service.ResourceMetadataService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class MessagePolicyExecutorTask implements PolicyExecutorTask {

    private final PasSettings settings;

    private final PoliciesService policiesService;

    private final ApplicationEventPublisher publisher;

    private final ResourceMetadataService resourceMetadataService;


    @Autowired
    public MessagePolicyExecutorTask(
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
    public void execute() {
        log.info("MessagePolicyExecutorTask started");
        fetchMessagePolicies()
        .collectList()
        .subscribe(
                results -> {
                    results.forEach(mp -> {
                        notifyOwners(mp,"PrimaryOwner");  
                        notifyOwners(mp,"SecondaryOwner");  
                    });
                    log.info("MessagePolicyExecutorTask completed");
                    log.info("-- {} message policies executed.", results.size());
                },
                error -> {
                    log.error("MessagePolicyExecutorTask terminated with error", error);
                }
                );
    }

    protected Flux<MessagePolicy> fetchMessagePolicies() {
        return
                policiesService
                .findAllMessagePolicies()
                .flatMapMany(policy -> Flux.fromIterable(policy.getMessagePolicies()));
    }

    private Mono<List<String>> fetchRecipientList(MessagePolicy messagePolicy, String ownerLabel){
        return 
                resourceMetadataService.getResources("organizations",ownerLabel)
                .flatMapMany(resources -> Flux.fromIterable(resources.getResources()))
                .delayElements(Duration.ofMillis(250))
                .filter(resource -> isBlacklisted(resource.getName()))
                .filter(resource -> isWhitelisted(messagePolicy, resource.getName()))
                .map(resource -> new String(resource.getMetadata().getLabels().get(ownerLabel) + "@pivotal.io"))
                .collectList();
    }

    private void notifyOwners(MessagePolicy messagePolicy, String ownerLabel) {
        fetchRecipientList(messagePolicy,ownerLabel)
            .doOnNext(
                recepient -> {
                    publisher.publishEvent(
                        new EmailNotificationEvent(this)
                            .domain(settings.getAppsDomain())
                            .from(messagePolicy.getOwnerTemplate().getFrom())
                            .recipients(recepient)
                            .subject(messagePolicy.getOwnerTemplate().getSubject())
                            .body(messagePolicy.getOwnerTemplate().getBody()));
                }
            )
        .subscribe();
    }

    private boolean isBlacklisted(String organization) {
        return !settings.getOrganizationBlackList().contains(organization);
    }


    private boolean isWhitelisted(MessagePolicy policy, String organization) {
        Set<String> prunedSet = new HashSet<>(policy.getOrganizationWhiteList());
        while (prunedSet.remove(""));
        Set<String> whitelist =
                CollectionUtils.isEmpty(prunedSet) ?
                        prunedSet: policy.getOrganizationWhiteList();
        return
                whitelist.isEmpty() ? true: policy.getOrganizationWhiteList().contains(organization);
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
        execute();
    }

}