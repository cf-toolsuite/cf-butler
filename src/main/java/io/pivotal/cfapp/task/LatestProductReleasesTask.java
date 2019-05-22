package io.pivotal.cfapp.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.client.PivnetClient;
import io.pivotal.cfapp.domain.product.PivnetCache;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LatestProductReleasesTask implements ApplicationRunner {

    private final PivnetClient client;
    private final PivnetCache cache;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public LatestProductReleasesTask(
            PivnetClient client,
            PivnetCache cache,
    		ApplicationEventPublisher publisher) {
        this.client = client;
        this.cache = cache;
        this.publisher = publisher;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        collect();
    }

    public void collect() {
        log.info("LatestProductReleasesTask started");
        client
            .getLatestProductReleases()
                .collectList()
                .subscribe(
                    r -> {
                        cache.setLatestProductReleases(r);
                        publisher.publishEvent(new LatestProductReleasesRetrievedEvent(this).latestReleases(r));
                        log.info("LatestProductReleasesTask completed");
                    }
                );
    }

    @Scheduled(cron = "${cron.collection}")
    protected void runTask() {
        collect();
    }
}