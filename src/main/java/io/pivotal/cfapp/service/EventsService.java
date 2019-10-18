package io.pivotal.cfapp.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import io.pivotal.cfapp.config.PasSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.Event;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.event.EventType;
import io.pivotal.cfapp.domain.event.Events;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
// @see https://apidocs.cloudfoundry.org/287/events/list_all_events.html
public class EventsService {

    private static final int EVENTS_PER_PAGE = 10;
    private final WebClient webClient;
    private final DefaultConnectionContext connectionContext;
    private final TokenProvider tokenProvider;
    private final PasSettings settings;
    private final ObjectMapper mapper;

    @Autowired
    public EventsService(
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
        final int pageSize = getPageSize(numberOfEvents);
        final String uri = UriComponentsBuilder
            .newInstance()
                .scheme("https")
                .host(settings.getApiHost())
                .path("/v2/events")
                .queryParam("q", "actee:{id}")
                .queryParam("page", 1)
                .queryParam("results-per-page", "{pageSize}")
                .queryParam("order-direction", "desc")
                .queryParam("order-by", "timestamp")
                .buildAndExpand(id, pageSize)
                .encode()
                .toUriString();
        return
            getOauthToken()
                .flatMap(t -> webClient
                                .get()
                                    .uri(uri)
                                    .header(HttpHeaders.AUTHORIZATION, t)
                                        .retrieve()
                                            .bodyToMono(String.class));
    }

    public Mono<String> getEvent(String id, String type) {
        Assert.hasText(id, "Global unique identifier for application or service instance must not be blank or null!");
        EventType eventType = EventType.from(type);
        final String uri = UriComponentsBuilder
            .newInstance()
                .scheme("https")
                .host(settings.getApiHost())
                .path("/v2/events")
                .queryParam("q", "actee:{id}")
                .queryParam("q", "type:{type}")
                .queryParam("page", 1)
                .queryParam("results-per-page", 1)
                .queryParam("order-direction", "desc")
                .queryParam("order-by", "timestamp")
                .buildAndExpand(id, eventType.getId())
                .encode()
                .toUriString();
        return
            getOauthToken()
                .flatMap(t -> webClient
                                .get()
                                    .uri(uri)
                                    .header(HttpHeaders.AUTHORIZATION, t)
                                        .retrieve()
                                            .bodyToMono(String.class));
    }

    public Flux<Event> getEvents(String id, String[] types) {
        return Flux
                .fromArray(types)
                .concatMap(type -> getEvent(id, type))
                .flatMap(json -> toFlux(json));
    }

    public Mono<Boolean> isDormantApplication(AppDetail detail, int daysSinceLastUpdate) {
        // @see https://docs.cloudfoundry.org/running/managing-cf/audit-events.html#considerations
        Mono<Boolean> result = Mono.just(Boolean.FALSE);
        if (daysSinceLastUpdate == -1) {
            result = Mono.just(Boolean.TRUE);
        } else if (daysSinceLastUpdate > settings.getEventsRetentionInDays()) {
            if (detail.getLastEventTime() != null) {
                result =  Mono.just(ChronoUnit.DAYS.between(detail.getLastPushed(), LocalDateTime.now()) >= daysSinceLastUpdate);
            }
        } else {
            String[] types = new String[] { EventType.AUDIT_APP_CREATE.getId(), EventType.AUDIT_APP_UPDATE.getId(), EventType.AUDIT_APP_RESTAGE.getId() };
            result = getEvents(detail.getAppId(), types)
                        .filter(event -> ChronoUnit.DAYS.between(event.getTime(), LocalDateTime.now()) >= daysSinceLastUpdate)
                        .collect(Collectors.toList())
                        .map(list -> list.size() > 0);
        }
        return result;
    }

    public Mono<Boolean> isDormantServiceInstance(ServiceInstanceDetail detail, int daysSinceLastUpdate) {
        // @see https://docs.cloudfoundry.org/running/managing-cf/audit-events.html#considerations
        Mono<Boolean> result = Mono.just(Boolean.FALSE);
        if (daysSinceLastUpdate == -1) {
            result = Mono.just(Boolean.TRUE);
        } else if (daysSinceLastUpdate > settings.getEventsRetentionInDays()) {
            if (detail.getLastUpdated() != null) {
                result = Mono.just(ChronoUnit.DAYS.between(detail.getLastUpdated(), LocalDateTime.now()) >= daysSinceLastUpdate);
            }
        } else {
            String[] types = new String[] { EventType.AUDIT_SERVICE_INSTANCE_CREATE.getId(), EventType.AUDIT_SERVICE_INSTANCE_UPDATE.getId(),
                EventType.AUDIT_USER_PROVIDED_SERVICE_INSTANCE_CREATE.getId(), EventType.AUDIT_USER_PROVIDED_SERVICE_INSTANCE_UPDATE.getId() };
            result = getEvents(detail.getServiceInstanceId(), types)
                        .filter(event -> ChronoUnit.DAYS.between(event.getTime(), LocalDateTime.now()) >= daysSinceLastUpdate)
                        .collect(Collectors.toList())
                        .map(list -> list.size() > 0);
        }
        return result;
    }

    public Flux<Event> toFlux(String json) {
        Flux<Event> result = Flux.empty();
        try {
            Events events = mapper.readValue(json, Events.class);
            result =
                Flux
                    .fromIterable(events.getResources())
                    .map(resource -> resource.getEntity())
                    .map(entity -> Event
                                        .builder()
                                            .type(entity.getType())
                                            .actee(entity.getActee())
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

    private static Integer getPageSize(Integer numberOfEvents) {
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