package io.pivotal.cfapp.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;

import reactor.core.publisher.Mono;

public class TkServiceUtil {

    private static final String LAST_TIME_COLLECTED = "Last-Time-Collected";

    private final TkService tkService;

    public TkServiceUtil(TkService tkService) {
        this.tkService = tkService;
    }

    public Mono<HttpHeaders> getHeaders() {
        return tkService
                .findOne()
                .map(lc -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(LAST_TIME_COLLECTED, lc.toString());
                    return headers;
                });
    }

    public Mono<LocalDateTime> getTimeCollected() {
        return tkService
                .findOne();
    }

}