package org.cftoolsuite.cfapp.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import org.cftoolsuite.cfapp.config.PasSettings;
import org.cftoolsuite.cfapp.domain.AppDetail;
import org.cftoolsuite.cfapp.domain.Event;
import org.cftoolsuite.cfapp.domain.ServiceInstanceDetail;
import org.cftoolsuite.cfapp.domain.event.EventType;
import org.cftoolsuite.cfapp.domain.event.Events;
import org.cftoolsuite.cfapp.domain.event.Resource;
import org.cftoolsuite.cfapp.util.RetryableTokenProvider;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
// @see https://apidocs.cloudfoundry.org/287/events/list_all_events.html
public class EventsService {

    private static final int EVENTS_PER_PAGE = 10;
    private static final int MAX_NUMBER_OF_EVENTS = 250;

    private final WebClient webClient;
    private final DefaultConnectionContext connectionContext;
    private final TokenProvider tokenProvider;
    private final PasSettings settings;

    @Autowired
    public EventsService(
            WebClient webClient,
            DefaultConnectionContext connectionContext,
            TokenProvider tokenProvider,
            PasSettings settings) {
        this.webClient = webClient;
        this.connectionContext = connectionContext;
        this.tokenProvider = tokenProvider;
        this.settings = settings;
    }

    public Mono<Events> getEvents(String id, String type) {
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
            RetryableTokenProvider
                .getToken(tokenProvider, connectionContext)
                .flatMap(t -> webClient
                        .get()
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, t)
                        .retrieve()
                        .bodyToMono(Events.class))
                        .timeout(settings.getTimeout(), Mono.just(Events.builder().build()))
                        .onErrorResume(
                            WebClientResponseException.class,
                            e -> {
                                log.warn(String.format("Could not obtain events from GET %s", uri), e);
                                return Mono.just(Events.builder().build());
                            }
                        );
    }

    public Mono<Events> getEvents(String id, Integer numberOfEvents) {
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
            RetryableTokenProvider
                .getToken(tokenProvider, connectionContext)
                .flatMap(t -> webClient
                        .get()
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, t)
                        .retrieve()
                        .bodyToMono(Events.class))
                        .timeout(settings.getTimeout(), Mono.just(Events.builder().build()))
                        .onErrorResume(
                            WebClientResponseException.class,
                            e -> {
                                log.warn(String.format("Could not obtain events from GET %s", uri), e);
                                return Mono.just(Events.builder().build());
                            }
                        );
    }

    public Flux<Event> getEvents(String id, String[] types) {
        return Flux
                .fromArray(types)
                .concatMap(type -> getEvents(id, type))
                .flatMap(this::toFlux)
                .onBackpressureBuffer();
    }

    public Mono<Boolean> isDormantApplication(AppDetail detail, int daysSinceLastUpdate) {
        if (daysSinceLastUpdate == -1) {
            return Mono.just(Boolean.TRUE);
        } else {
            return getEvents(detail.getAppId(), 1)
                    .flatMap(
                            envelope -> envelope.hasNoEvents()
                            ? isDormant(detail.getLastPushed(), daysSinceLastUpdate)
                                    : toFlux(envelope)
                                    .filter(event -> ChronoUnit.DAYS.between(event.getTime(), LocalDateTime.now()) >= daysSinceLastUpdate)
                                    .collect(Collectors.toList())
                                    .map(list -> list.size() > 0)
                            );
        }
    }

    public Mono<Boolean> isDormantServiceInstance(ServiceInstanceDetail detail, int daysSinceLastUpdate) {
        if (daysSinceLastUpdate == -1) {
            return Mono.just(Boolean.TRUE);
        } else {
            return getEvents(detail.getServiceInstanceId(), 1)
                    .flatMap(
                            envelope -> envelope.hasNoEvents()
                            ? isDormant(detail.getLastUpdated(), daysSinceLastUpdate)
                                    : toFlux(envelope)
                                    .filter(event -> ChronoUnit.DAYS.between(event.getTime(), LocalDateTime.now()) >= daysSinceLastUpdate)
                                    .collect(Collectors.toList())
                                    .map(list -> list.size() > 0)
                            );
        }
    }

    public Flux<Event> toFlux(Events envelope) {
        return
                Flux
                .fromIterable(envelope.getResources())
                .map(Resource::getEntity)
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
    }

    private Integer getPageSize(Integer numberOfEvents) {
        Integer result = EVENTS_PER_PAGE;
        if (numberOfEvents != null) {
            result = numberOfEvents;
        }
        Assert.isTrue(result > 0, "Number of events requested must be greater than zero!");
        Assert.isTrue(result <= MAX_NUMBER_OF_EVENTS, String.format("The maximum number of events that may be requested is %d!", MAX_NUMBER_OF_EVENTS));
        return result;
    }

    private Mono<Boolean> isDormant(LocalDateTime dateTime, int daysSinceLastUpdate) {
        Mono<Boolean> result = Mono.just(Boolean.TRUE);
        if (dateTime != null) {
            result = Mono.just(ChronoUnit.DAYS.between(dateTime, LocalDateTime.now()) >= daysSinceLastUpdate);
        }
        return result;
    }

}
