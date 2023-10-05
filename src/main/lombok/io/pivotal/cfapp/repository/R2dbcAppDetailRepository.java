package io.pivotal.cfapp.repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Repository
public class R2dbcAppDetailRepository {

    private final R2dbcEntityOperations client;

    @Autowired
    public R2dbcAppDetailRepository(R2dbcEntityOperations client) {
        this.client = client;
    }

    public Mono<Void> deleteAll() {
        return
            client
                .delete(AppDetail.class)
                .all()
                .then();
    }

    public Flux<AppDetail> findAll() {
        Sort order = Sort.by(Order.asc("organization"), Order.asc("space"), Order.asc("app_name"));
        return
            client
                .select(AppDetail.class)
                .matching(Query.empty().sort(order))
                .all();
    }

    private Flux<Tuple2<AppDetail, ApplicationPolicy>> findApplicationsThatDoNotHaveServiceBindings(ApplicationPolicy policy) {
        LocalDateTime fromDateTime = policy.getOption("from-datetime", LocalDateTime.class);
        String fromDuration = policy.getOption("from-duration", String.class);
        LocalDateTime temporal = null;
        Criteria criteria = null;
        if (fromDateTime != null) {
            temporal = fromDateTime;
        }
        if (fromDuration != null) {
            temporal = LocalDateTime.now().minus(Duration.parse(fromDuration));
        }
        if (temporal != null) {
            criteria = Criteria.where("requested_state").is(policy.getState()).and("service_instance_id").isNull().and("last_event_time").lessThanOrEquals(temporal);
        } else {
            criteria = Criteria.where("requested_state").is(policy.getState()).and("service_instance_id").isNull();
        }
        return
            client
                .select(AppDetail.class)
                .from("service_bindings")
                .matching(Query.query(criteria).sort(Sort.by(Order.asc("organization"), Order.asc("space"), Order.asc("app_name"))))
                .all()
                .map(r -> toTuple(r, policy));
    }

    private Flux<Tuple2<AppDetail, ApplicationPolicy>> findApplicationsThatMayHaveServiceBindings(ApplicationPolicy policy) {
        LocalDateTime fromDateTime = policy.getOption("from-datetime", LocalDateTime.class);
        String fromDuration = policy.getOption("from-duration", String.class);
        LocalDateTime temporal = null;
        Criteria criteria = null;
        if (fromDateTime != null) {
            temporal = fromDateTime;
        }
        if (fromDuration != null) {
            temporal = LocalDateTime.now().minus(Duration.parse(fromDuration));
        }
        if (temporal != null) {
            criteria = Criteria.where("requested_state").is(policy.getState()).and("last_event_time").lessThanOrEquals(temporal);
        } else {
            criteria = Criteria.where("requested_state").is(policy.getState());
        }
        return
            client
                .select(AppDetail.class)
                .matching(Query.query(criteria).sort(Sort.by(Order.asc("organization"), Order.asc("space"), Order.asc("app_name"))))
                .all()
                .map(r -> toTuple(r, policy));
    }

    public Mono<AppDetail> findByAppId(String appId) {
        Criteria criteria =
                Criteria
                .where("app_id").is(appId);
        return
            client
                .select(AppDetail.class)
                .matching(Query.query(criteria))
                .one();
    }

    public Flux<Tuple2<AppDetail, ApplicationPolicy>> findByApplicationPolicy(ApplicationPolicy policy, boolean mayHaveServiceBindings) {
        return mayHaveServiceBindings == true
                ? findApplicationsThatMayHaveServiceBindings(policy)
                        : findApplicationsThatDoNotHaveServiceBindings(policy);
    }

    public Flux<AppDetail> findByDateRange(LocalDate start, LocalDate end) {
        Criteria criteria =
                Criteria
                .where("last_pushed")
                .lessThanOrEquals(LocalDateTime.of(end, LocalTime.MAX))
                .and("last_pushed")
                .greaterThan(LocalDateTime.of(start, LocalTime.MIDNIGHT));
        Sort order = Sort.by(Order.desc("last_pushed"));
        return
            client
                .select(AppDetail.class)
                .matching(Query.query(criteria).sort(order))
                .all();
    }

    public Mono<AppDetail> save(AppDetail entity) {
        return
            client
                .insert(entity);
    }

    private Tuple2<AppDetail, ApplicationPolicy> toTuple(AppDetail detail, ApplicationPolicy policy) {
        return Tuples.of(detail, policy);
    }

}
