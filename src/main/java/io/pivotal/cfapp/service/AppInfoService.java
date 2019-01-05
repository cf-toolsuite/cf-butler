package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.AppDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AppInfoService {

	Mono<Void> deleteAll();

	Mono<AppDetail> save(AppDetail entity);

	Flux<AppDetail> findAll();

}