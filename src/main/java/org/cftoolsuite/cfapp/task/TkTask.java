package org.cftoolsuite.cfapp.task;

import org.cftoolsuite.cfapp.event.DatabaseCreatedEvent;
import org.cftoolsuite.cfapp.event.TkRetrievedEvent;
import org.cftoolsuite.cfapp.service.TimeKeeperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TkTask implements ApplicationListener<DatabaseCreatedEvent> {

    private final TimeKeeperService tkService;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public TkTask(
            TimeKeeperService tkService,
            ApplicationEventPublisher publisher) {
        this.tkService = tkService;
        this.publisher = publisher;
    }

    public void collect() {
        log.info("TkTask started");
        tkService
            .deleteOne()
            .then(tkService.save())
            .then(tkService.findOne())
            .subscribe(
                result -> {
                    publisher.publishEvent(new TkRetrievedEvent(this).lastCollected(result));
                    log.info("TkTask completed");
                    log.trace("Last collected time was set to {}", result);
                },
                error -> {
                    log.error("TkTask terminated with error", error);
                }
            );
    }

    @Override
    public void onApplicationEvent(DatabaseCreatedEvent event) {
        collect();
    }

    @Scheduled(cron = "${cron.collection}")
    protected void runTask() {
        collect();
    }

}
