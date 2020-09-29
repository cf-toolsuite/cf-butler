package io.pivotal.cfapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.AppRelationship;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcAppRelationshipRepository {

    private final R2dbcEntityOperations client;

    @Autowired
    public R2dbcAppRelationshipRepository(R2dbcEntityOperations client) {
        this.client = client;
    }

    public Mono<Void> deleteAll() {
        return
                client
                .delete(AppRelationship.class)
                .all()
                .then();
    }

    public Flux<AppRelationship> findAll() {
        return
                client
                .select(AppRelationship.class)
                .matching(Query.empty().sort(Sort.by(Order.asc("organization"), Order.asc("space"), Order.asc("app_name"))))
                .all();
    }

    public Flux<AppRelationship> findByApplicationId(String applicationId) {
        Criteria criteria =
                Criteria.where("app_id").is(applicationId);
        return
                client
                .select(AppRelationship.class)
                .matching(Query.query(criteria).sort(Sort.by(Order.asc("organization"), Order.asc("space"), Order.asc("service_name"))))
                .all();
    }

    public Flux<AppRelationship> findByServiceInstanceId(String serviceInstanceId) {
        Criteria criteria =
                Criteria.where("service_instance_id").is(serviceInstanceId);
        return
                client
                .select(AppRelationship.class)
                .matching(Query.query(criteria).sort(Sort.by(Order.asc("organization"), Order.asc("space"), Order.asc("service_name"))))
                .all();
    }

    public Mono<AppRelationship> save(AppRelationship entity) {
        return
                client
                .insert(entity);
    }

}
