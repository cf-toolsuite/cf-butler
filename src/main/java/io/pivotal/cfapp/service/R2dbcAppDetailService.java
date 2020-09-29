package io.pivotal.cfapp.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.repository.R2dbcAppDetailRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Slf4j
@Service
public class R2dbcAppDetailService implements AppDetailService {

    private R2dbcAppDetailRepository repo;

    @Autowired
    public R2dbcAppDetailService(R2dbcAppDetailRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public Mono<Void> deleteAll() {
        return repo.deleteAll();
    }

    @Override
    public Flux<AppDetail> findAll() {
        return repo.findAll();
    }

    @Override
    public Mono<AppDetail> findByAppId(String appId) {
        return repo.findByAppId(appId);
    }

    @Override
    public Flux<Tuple2<AppDetail, ApplicationPolicy>> findByApplicationPolicy(ApplicationPolicy policy, boolean mayHaveServiceBindings) {
        return repo.findByApplicationPolicy(policy, mayHaveServiceBindings);
    }

    @Override
    public Flux<AppDetail> findByDateRange(LocalDate start, LocalDate end) {
        return repo.findByDateRange(start, end);
    }

    @Override
    @Transactional
    public Mono<AppDetail> save(AppDetail entity) {
        return repo
                .save(entity)
                .onErrorContinue(
                        (ex, data) -> log.error(String.format("Problem saving application %s.", entity), ex));
    }

}
