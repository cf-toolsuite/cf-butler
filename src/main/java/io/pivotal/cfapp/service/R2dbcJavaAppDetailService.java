package io.pivotal.cfapp.service;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pivotal.cfapp.domain.JavaAppDetail;
import io.pivotal.cfapp.repository.R2dbcJavaAppDetailRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class R2dbcJavaAppDetailService implements JavaAppDetailService {

    private R2dbcJavaAppDetailRepository repo;

    @Autowired
    public R2dbcJavaAppDetailService(R2dbcJavaAppDetailRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public Mono<Void> deleteAll() {
        return repo.deleteAll();
    }

    @Override
    public Flux<JavaAppDetail> findAll() {
        return repo.findAll();
    }

    @Override
    public Flux<Map<String, String>> findSpringApplications() {
        return repo
                .findAll()
                .filter(jad -> StringUtils.isNotBlank(jad.getSpringDependencies()))
                .map(jad ->
                    Map.of("organization", jad.getOrganization(),
                        "space", jad.getSpace(),
                        "appId", jad.getAppId(),
                        "appName", jad.getAppName(),
                        "dropletId", jad.getDropletId(),
                        "springDependencies", jad.getSpringDependencies().replace("\n", ", ")
                    )
                );
    }

    @Override
    public Mono<JavaAppDetail> findByAppId(String appId) {
        return repo.findByAppId(appId);
    }

    @Override
    @Transactional
    public Mono<JavaAppDetail> save(JavaAppDetail entity) {
        return repo
                .save(entity)
                .onErrorContinue(
                        (ex, data) -> log.error(String.format("Problem saving Java application %s.", entity), ex));
    }

}
