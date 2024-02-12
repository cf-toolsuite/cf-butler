package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.JavaAppDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface JavaAppDetailService {

    Mono<Void> deleteAll();

    Flux<JavaAppDetail> findAll();

    Mono<JavaAppDetail> findByAppId(String appId);

    Mono<JavaAppDetail> save(JavaAppDetail entity);
}
