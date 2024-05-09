package org.cftoolsuite.cfapp.task;

import org.cftoolsuite.cfapp.domain.Policies;
import org.cftoolsuite.cfapp.domain.Policy;
import org.cftoolsuite.cfapp.event.PoliciesLoadedEvent;
import org.cftoolsuite.cfapp.service.PoliciesService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@EnableScheduling
public class PolicyExecutorTaskScheduler implements ApplicationListener<PoliciesLoadedEvent> {

    private final BeanFactory factory;
    private final PoliciesService service;
    private final TaskScheduler scheduler;

    @Autowired
    public PolicyExecutorTaskScheduler(
        BeanFactory factory,
        PoliciesService service,
        TaskScheduler scheduler) {
        this.factory = factory;
        this.service = service;
        this.scheduler = scheduler;
    }

    public void scheduleTasks(Policies policies) {
        log.info("PolicyExecutorTaskScheduler started");
        service
            .getTaskMap()
            .flatMapMany(taskTypeMap ->
                Flux.fromIterable(taskTypeMap.entrySet())
                    .flatMap(entry -> {
                        String policyId = entry.getKey();
                        Class<? extends PolicyExecutorTask> taskClass = entry.getValue();
                        PolicyExecutorTask task = factory.getBean(taskClass);
                        Policy policy = policies.getById(policyId);
                        return
                            Mono
                                .fromRunnable(() ->
                                    scheduler.schedule(() ->
                                        task.execute(policyId), new CronTrigger(policy.getCronExpression())
                                    )
                                );
                    }))
            .count()
            .subscribe(
                result -> {
                    log.info("PolicyExecutorTaskScheduler completed. {} policies scheduled.", result);
                },
                error -> {
                    log.error("PolicyExecutorTaskScheduler terminated with error", error);
                }
            );
    }

    @Override
    public void onApplicationEvent(PoliciesLoadedEvent event) {
        scheduleTasks(event.getPolicies());
    }

}
