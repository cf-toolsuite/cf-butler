package io.pivotal.cfapp.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import io.pivotal.cfapp.repository.R2dbcServiceInstanceDetailRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Slf4j
@Service
public class R2dbcServiceInstanceDetailService implements ServiceInstanceDetailService {

    private R2dbcServiceInstanceDetailRepository repo;

    public R2dbcServiceInstanceDetailService(R2dbcServiceInstanceDetailRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public Mono<Void> deleteAll() {
        return repo.deleteAll();
    }

    @Override
    public Flux<ServiceInstanceDetail> findAll() {
        return repo.findAll();
    }

    @Override
    public Flux<ServiceInstanceDetail> findByDateRange(LocalDate start, LocalDate end) {
        return repo.findByDateRange(start, end);
    }

    @Override
    public Flux<Tuple2<ServiceInstanceDetail, ServiceInstancePolicy>> findByServiceInstancePolicy(
            ServiceInstancePolicy policy) {
        return repo.findByServiceInstancePolicy(policy);
    }

    @Override
    @Transactional
    public Mono<ServiceInstanceDetail> save(ServiceInstanceDetail entity) {
        return repo
                .save(entity)
                .onErrorContinue(
                        (ex, data) -> log.error(String.format("Problem saving service instance %s.", entity), ex));
    }

}
