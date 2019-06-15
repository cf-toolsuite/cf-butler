package io.pivotal.cfapp.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.client.PivnetClient;
import io.pivotal.cfapp.domain.product.PivnetCache;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@ConditionalOnProperty(name = "pivnet.enabled", havingValue = "true")
public class ProductsAndReleasesTask implements ApplicationRunner {

    private final PivnetClient client;
    private final PivnetCache cache;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public ProductsAndReleasesTask(
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
        log.info("ProductsAndReleasesTask started");
        client
            .getProducts()
            .flatMap(products -> {
                cache.setProducts(products);
                return Mono.empty(); })
            .then(
                client
                    .getAllProductReleases()
                    .collectList()
                    .flatMap(releases -> {
                        cache.setAllProductReleases(releases);
                        return Mono.empty(); }))
            .then(
                client
                    .getLatestProductReleases()
                    .collectList()
                    .flatMap(releases -> { cache.setLatestProductReleases(releases); return Mono.empty(); }))
            .subscribe(
                result -> {
                    publisher.publishEvent(
                        new ProductsAndReleasesRetrievedEvent(this)
                                .products(cache.getProducts())
                                .allReleases(cache.getAllProductReleases())
                                .latestReleases(cache.getLatestProductReleases()));
                    log.info("ProductsAndReleasesTask completed");
                },
                error -> {
                    log.error("ProductsAndReleasesTask completed with error", error);
                }
            );
    }

    @Scheduled(cron = "${cron.collection}")
    protected void runTask() {
        collect();
    }
}