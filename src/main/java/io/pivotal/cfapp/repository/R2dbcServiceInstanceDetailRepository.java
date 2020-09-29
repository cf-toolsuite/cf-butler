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

import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import io.pivotal.cfapp.domain.ServiceInstancePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Repository
public class R2dbcServiceInstanceDetailRepository {

    private final R2dbcEntityOperations client;

    @Autowired
    public R2dbcServiceInstanceDetailRepository(R2dbcEntityOperations client) {
        this.client = client;
    }

    public Mono<Void> deleteAll() {
        return
                client
                .delete(ServiceInstanceDetail.class)
                .all()
                .then();
    }

    public Flux<ServiceInstanceDetail> findAll() {
        return
                client
                .select(ServiceInstanceDetail.class)
                .matching(Query.empty().sort(Sort.by(Order.asc("organization"), Order.asc("space"), Order.asc("service"), Order.asc("service_name"))))
                .all();
    }

    public Flux<ServiceInstanceDetail> findByDateRange(LocalDate start, LocalDate end) {
        Criteria criteria =
                Criteria.where("last_updated").lessThanOrEquals(LocalDateTime.of(end, LocalTime.MAX)).and("last_updated").greaterThan(LocalDateTime.of(start, LocalTime.MIDNIGHT));
        return
                client
                .select(ServiceInstanceDetail.class)
                .matching(Query.query(criteria).sort(Sort.by(Order.desc("last_updated"))))
                .all();
    }

    public Flux<Tuple2<ServiceInstanceDetail, ServiceInstancePolicy>> findByServiceInstancePolicy(ServiceInstancePolicy policy) {
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
            criteria = Criteria.where("bound_applications").isNull().and("last_updated").lessThanOrEquals(temporal);
        } else {
            criteria = Criteria.where("bound_applications").isNull();
        }
        return
                client
                .select(ServiceInstanceDetail.class)
                .matching(Query.query(criteria).sort(Sort.by(Order.asc("organization"), Order.asc("space"), Order.asc("service_name"))))
                .all()
                .map(r -> toTuple(r, policy));
    }

    public Mono<ServiceInstanceDetail> save(ServiceInstanceDetail entity) {
        return
                client
                .insert(entity);
    }

    private Tuple2<ServiceInstanceDetail, ServiceInstancePolicy> toTuple(ServiceInstanceDetail detail, ServiceInstancePolicy policy) {
        return Tuples.of(detail, policy);
    }

}
