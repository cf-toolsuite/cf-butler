package io.pivotal.cfapp.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.service.TkService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TkTask implements ApplicationListener<DatabaseCreatedEvent> {

    private final TkService tkService;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public TkTask(
            TkService tkService,
    		ApplicationEventPublisher publisher) {
        this.tkService = tkService;
        this.publisher = publisher;
    }

    @Override
	public void onApplicationEvent(DatabaseCreatedEvent event) {
		collect();
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

    @Scheduled(cron = "${cron.collection}")
    protected void runTask() {
        collect();
    }

}
