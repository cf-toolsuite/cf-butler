package io.pivotal.cfapp.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.service.TkService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TkTask implements ApplicationRunner {

    private final TkService tkService;
    private ApplicationEventPublisher publisher;

    @Autowired
    public TkTask(
            TkService tkService,
    		ApplicationEventPublisher publisher) {
        this.tkService = tkService;
        this.publisher = publisher;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        collect();
    }

    public void collect() {
        log.info("TkTask started");
        tkService
            .deleteOne()
            .then(tkService.save())
            .then(tkService.findOne())
                .subscribe(
                    r -> {
                        publisher.publishEvent(new TkRetrievedEvent(this).lastCollected(r));
                        log.info("TkTask completed");
                    }
                );
    }

    @Scheduled(cron = "${cron.collection}")
    protected void runTask() {
        collect();
    }

}
