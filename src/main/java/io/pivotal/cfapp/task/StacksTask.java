package io.pivotal.cfapp.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.stacks.Stack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.repository.StacksCache;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class StacksTask implements ApplicationListener<TkRetrievedEvent> {

    private final DefaultCloudFoundryOperations opsClient;
    private final ObjectMapper mapper;
    private final StacksCache cache;


    @Autowired
    public StacksTask(
        DefaultCloudFoundryOperations opsClient,
        ObjectMapper mapper,
        StacksCache cache) {
        this.opsClient = opsClient;
        this.mapper = mapper;
        this.cache = cache;
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
                    log.trace(mapWithException("StacksCache", result));
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

    private String mapWithException(String type, Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Problem mapping " + type);
        }
    }

}