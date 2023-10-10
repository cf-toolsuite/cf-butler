package io.pivotal.cfapp.service;

import java.time.LocalDate;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface AppDetailService {

    Mono<Void> deleteAll();

    Flux<AppDetail> findAll();

    Mono<AppDetail> findByAppId(String appId);

    Flux<Tuple2<AppDetail, ApplicationPolicy>> findByApplicationPolicy(ApplicationPolicy policy, boolean mayHaveServiceBindings);

    Flux<AppDetail> findByDateRange(LocalDate start, LocalDate end);

    Mono<AppDetail> save(AppDetail entity);
}
