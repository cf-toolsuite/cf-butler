package org.cftoolsuite.cfapp.task;

import org.cftoolsuite.cfapp.event.BuildpacksRetrievedEvent;
import org.cftoolsuite.cfapp.event.TkRetrievedEvent;
import org.cftoolsuite.cfapp.service.BuildpacksCache;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.buildpacks.Buildpack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class BuildpacksTask implements ApplicationListener<TkRetrievedEvent> {

    private final DefaultCloudFoundryOperations opsClient;
    private final BuildpacksCache cache;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public BuildpacksTask(
            DefaultCloudFoundryOperations opsClient,
            BuildpacksCache cache,
            ApplicationEventPublisher publisher) {
        this.opsClient = opsClient;
        this.cache = cache;
        this.publisher = publisher;
    }

    public void collect() {
        log.info("BuildpacksTask started");
        getBuildpacks()
        .collectList()
        .map(cache::from)
        .subscribe(
            result -> {
                publisher.publishEvent(new BuildpacksRetrievedEvent(this));
                log.trace("Buildpack cache contains {}", result);
                log.info("BuildpacksTask completed. {} buildpacks found.", result.size());
            },
            error -> {
                log.error("BuildpacksTask terminated with error", error);
            }
        );
    }

    protected Flux<Buildpack> getBuildpacks() {
        return opsClient
                .buildpacks()
                .list();
    }

    @Override
    public void onApplicationEvent(TkRetrievedEvent event) {
        collect();
    }

}
