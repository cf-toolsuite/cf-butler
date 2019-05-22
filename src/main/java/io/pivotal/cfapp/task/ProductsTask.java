package io.pivotal.cfapp.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.client.PivnetClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProductsTask implements ApplicationRunner {

    private final PivnetClient client;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public ProductsTask(
    		PivnetClient client,
    		ApplicationEventPublisher publisher) {
        this.client = client;
        this.publisher = publisher;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        collect();
    }

    public void collect() {
        log.info("ProductsTask started");
        client
            .getProducts()
                .subscribe(
                    r -> {
                        publisher.publishEvent(new ProductsRetrievedEvent(this).products(r));
                        log.info("ProductsTask completed");
                    }
                );
    }

    @Scheduled(cron = "${cron.collection}")
    protected void runTask() {
        collect();
    }
}