package io.pivotal.cfapp.task;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.stacks.Stack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.event.StacksRetrievedEvent;
import io.pivotal.cfapp.event.TkRetrievedEvent;
import io.pivotal.cfapp.service.StacksCache;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class StacksTask implements ApplicationListener<TkRetrievedEvent> {

    private final DefaultCloudFoundryOperations opsClient;
    private final StacksCache cache;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public StacksTask(
        DefaultCloudFoundryOperations opsClient,
        StacksCache cache,
        ApplicationEventPublisher publisher) {
        this.opsClient = opsClient;
        this.cache = cache;
        this.publisher = publisher;
    }


    @Override
    public void onApplicationEvent(TkRetrievedEvent event) {
        collect();
    }

    public void collect() {
        log.info("StacksTask started");
        getStacks()
            .collectList()
            .map(list -> cache.from(list))
            .subscribe(
                result -> {
                    publisher.publishEvent(new StacksRetrievedEvent(this));
                    log.trace("Stacks cache contains {}", result);
                    log.info("StacksTask completed");
                    log.trace("Retrieved {} stacks", result.size());
                },
                error -> {
                    log.error("StacksTask terminated with error", error);
                });
    }

    protected Flux<Stack> getStacks() {
        return opsClient
                .stacks()
                    .list();
    }

}