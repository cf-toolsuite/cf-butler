package io.pivotal.cfapp.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.AppEvent;
import io.pivotal.cfapp.domain.event.Events;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class AppEventsService {

    private static final int EVENTS_PER_PAGE = 10;
    private final WebClient webClient;
    private final DefaultConnectionContext connectionContext;
    private final TokenProvider tokenProvider;
    private final PasSettings settings;
    private final ObjectMapper mapper;

    @Autowired
    public AppEventsService(
        WebClient webClient,
        DefaultConnectionContext connectionContext,
        TokenProvider tokenProvider,
        PasSettings settings,
        ObjectMapper mapper) {
        this.webClient = webClient;
        this.connectionContext = connectionContext;
        this.tokenProvider = tokenProvider;
        this.settings = settings;
        this.mapper = mapper;
    }

    public Mono<String> getEvents(String id, Integer numberOfEvents) {
        Assert.hasText(id, "Global unique identifier for application or service instance must not be blank or null!");
        final String uri = "https://" + settings.getApiHost() + "/v2/events?q=actee:{id}&page=1&results-per-page{pageSize}&order-direction=desc&order-by=timestamp";
        final int pageSize = getPageSize(numberOfEvents);
        return
            getOauthToken()
                .flatMap(t -> webClient
                                .get()
                                    .uri(uri, id, pageSize)
                                    .header(HttpHeaders.AUTHORIZATION, t)
                                        .retrieve()
                                            .bodyToMono(String.class));
    }

    public Flux<AppEvent> toFlux(String json) {
        Flux<AppEvent> result = Flux.empty();
        try {
            Events events = mapper.readValue(json, Events.class);
            result =
                Flux
                    .fromIterable(events.getResources())
                    .map(resource -> resource.getEntity())
                    .map(entity -> AppEvent
                                        .builder()
                                            .name(entity.getType())
                                            .actor(entity.getActor())
                                            .time(
                                                entity.getTimestamp() != null
                                                    ? LocalDateTime.ofInstant(entity.getTimestamp(), ZoneOffset.UTC)
                                                    : null)
                                        .build()
                    );
        } catch (IOException ioe) {
            log.warn("Trouble mapping events. {}", ioe.getMessage());
        }
        return result;
    }

    private Mono<String> getOauthToken() {
        tokenProvider.invalidate(connectionContext);
        return tokenProvider.getToken(connectionContext);
    }

    private Integer getPageSize(Integer numberOfEvents) {
        int maxNumberOfEvents = 250;
        Integer result = EVENTS_PER_PAGE;
        if (numberOfEvents != null) {
            result = numberOfEvents;
        }
        Assert.isTrue(result > 0, "Number of events requested must be greater than zero!");
        Assert.isTrue(result <= maxNumberOfEvents, String.format("The maximum number of events that may be requested is %d!", maxNumberOfEvents));
        return result;
    }

}