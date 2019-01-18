package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface AppDetailService {

	Mono<Void> deleteAll();

	Mono<AppDetail> save(AppDetail entity);

	Flux<AppDetail> findAll();
		
	Flux<Tuple2<AppDetail, ApplicationPolicy>> findByApplicationPolicy(ApplicationPolicy policy, boolean mayHaveServiceBindings);

}