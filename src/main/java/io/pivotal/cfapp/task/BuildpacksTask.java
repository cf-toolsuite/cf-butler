package io.pivotal.cfapp.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.buildpacks.Buildpack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.event.BuildpacksRetrievedEvent;
import io.pivotal.cfapp.event.TkRetrievedEvent;
import io.pivotal.cfapp.service.BuildpacksCache;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class BuildpacksTask implements ApplicationListener<TkRetrievedEvent> {

    private final DefaultCloudFoundryOperations opsClient;
    private final ObjectMapper mapper;
    private final BuildpacksCache cache;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public BuildpacksTask(
        DefaultCloudFoundryOperations opsClient,
        ObjectMapper mapper,
        BuildpacksCache cache,
        ApplicationEventPublisher publisher) {
        this.opsClient = opsClient;
        this.mapper = mapper;
        this.cache = cache;
        this.publisher = publisher;
    }


    @Override
    public void onApplicationEvent(TkRetrievedEvent event) {
        collect();
    }

    public void collect() {
        log.info("BuildpacksTask started");
        getBuildpacks()
            .collectList()
            .map(list -> cache.from(list))
            .subscribe(
                result -> {
                    publisher.publishEvent(new BuildpacksRetrievedEvent(this));
                    log.trace(mapWithException("BuildpackCache", result));
                    log.info("BuildpacksTask completed");
                    log.trace("Retrieved {} buildpacks", result.size());
                },
                error -> {
                    log.error("BuildpacksTask terminated with error", error);
                });
    }

    protected Flux<Buildpack> getBuildpacks() {
        return opsClient
                .buildpacks()
                    .list();
    }

    private String mapWithException(String type, Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Problem mapping " + type);
        }
    }

}