package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AppDetailService {

	Mono<Void> deleteAll();

	Mono<AppDetail> save(AppDetail entity);

	Flux<AppDetail> findAll();
		
	Flux<AppDetail> findByApplicationPolicy(ApplicationPolicy policy, boolean mayHaveServiceBindings);

}