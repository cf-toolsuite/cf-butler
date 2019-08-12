package io.pivotal.cfapp.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.HygienePolicy;
import io.pivotal.cfapp.service.DormantWorkloadsService;
import io.pivotal.cfapp.service.PoliciesService;
import io.pivotal.cfapp.service.UserSpacesService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
@Component
public class HygienePolicyExecutorTask implements PolicyExecutorTask {

    private final PasSettings settings;
    private final PoliciesService policiesService;
    private final UserSpacesService userSpacesService;
    private final DormantWorkloadsService dormantWorkloadsService;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public HygienePolicyExecutorTask(
        PasSettings settings,
        PoliciesService poloiciesService,
        UserSpacesService userSpacesService,
        DormantWorkloadsService dormantWorkloadsService,
        ApplicationEventPublisher publisher
    ) {
        this.settings = settings;
        this.policiesService = poloiciesService;
        this.userSpacesService = userSpacesService;
        this.dormantWorkloadsService = dormantWorkloadsService;
        this.publisher = publisher;
    }

	@Override
    public void execute() {
		log.info("HygienePolicyExecutorTask started");
        fetchHygienePolicies()
            .concatMap(hp -> execute(hp).collectList().map(result -> Tuples.of(hp, result)))
            .collectList()
	    	.subscribe(
                results -> {
                    /*results.forEach(
                        result ->
                            publisher.publishEvent(
                                new EmailNotificationEvent(this)
                                    .domain(settings.getAppsDomain())
                                    .from(result.getT1().getEmailNotificationTemplate().getFrom())
                                    .recipients(result.getT1().getEmailNotificationTemplate().getTo())
                                    .subject(result.getT1().getEmailNotificationTemplate().getSubject())
                                    .body(result.getT1().getEmailNotificationTemplate().getBody())
                                    .attachmentContents(toMap(result.getT2()))
                            )
                    );*/
					log.info("HygienePolicyExecutorTask completed");
					log.info("-- {} hygiene policies executed.", results.size());
				},
				error -> {
					log.error("HygienePolicyExecutorTask terminated with error", error);
				}
			);
    }

    @Scheduled(cron = "${cron.execution}")
    protected void runTask() {
    	execute();
    }

	protected Flux<HygienePolicy> fetchHygienePolicies() {
        return
            policiesService
				.findAllQueryPolicies()
                .flatMapMany(policy -> Flux.fromIterable(policy.getHygienePolicies()));
    }

    protected Flux<Tuple2<String, String>> execute(HygienePolicy policy) {
        return null;
    }
}
