package io.pivotal.cfapp.service;

import java.time.LocalDate;

import io.pivotal.cfapp.domain.HistoricalRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface HistoricalRecordService {

    Flux<HistoricalRecord> findAll();
    Flux<HistoricalRecord> findByDateRange(LocalDate start, LocalDate end);
    Mono<HistoricalRecord> save(HistoricalRecord entity);
}
