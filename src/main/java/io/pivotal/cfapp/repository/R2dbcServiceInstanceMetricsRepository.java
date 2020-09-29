package io.pivotal.cfapp.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.Defaults;
import io.pivotal.cfapp.domain.ServiceInstanceDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Repository
public class R2dbcServiceInstanceMetricsRepository {

    private final R2dbcEntityOperations client;

    @Autowired
    public R2dbcServiceInstanceMetricsRepository(
            R2dbcEntityOperations client) {
        this.client = client;
    }

    protected Flux<Tuple2<String, Long>> by(String columnName) {
        String sql = "select " + columnName + ", count(" + columnName + ") as cnt from service_instance_detail group by " + columnName;
        return
                client
                .getDatabaseClient()
                .sql(sql)
                .map((row, metadata)
                        -> Tuples.of(Defaults.getColumnValueOrDefault(row, columnName, String.class, "--"), Defaults.getColumnValueOrDefault(row, "cnt", Long.class, 0L)))
                .all()
                .defaultIfEmpty(Tuples.of("--", 0L));
    }

    public Flux<Tuple2<String, Long>> byOrganization() {
        return by("organization");
    }

    public Flux<Tuple2<String, Long>> byService() {
        String sqlup = "select type, count(type) as cnt from service_instance_detail where type = 'user_provided_service_instance' group by type";
        Flux<Tuple2<String, Long>> ups =
                client
                .getDatabaseClient()
                .sql(sqlup)
                .map((row, metadata)
                        -> Tuples.of("user-provided", Defaults.getColumnValueOrDefault(row, "cnt", Long.class, 0L)))
                .all()
                .defaultIfEmpty(Tuples.of("user-provided", 0L));
        String sqlms = "select service, count(service) as cnt from service_instance_detail where type = 'managed_service_instance' group by service";
        Flux<Tuple2<String, Long>> ms =
                client
                .getDatabaseClient()
                .sql(sqlms)
                .map((row, metadata)
                        -> Tuples.of(Defaults.getColumnValueOrDefault(row, "service", String.class, "managed"), Defaults.getColumnValueOrDefault(row, "cnt", Long.class, 0L)))
                .all()
                .defaultIfEmpty(Tuples.of("managed", 0L));
        return ups.concatWith(ms);
    }

    public Flux<Tuple2<String, Long>> byServiceAndPlan() {
        String sql = "select service, plan, count(*) as cnt from service_instance_detail where type = 'managed_service_instance' group by service, plan";
        return
                client
                .getDatabaseClient()
                .sql(sql)
                .map((row, metadata)
                        -> Tuples.of(
                                String.join(
                                        "/",
                                        Defaults.getColumnValueOrDefault(row, "service", String.class, "unknown"),
                                        Defaults.getColumnValueOrDefault(row, "plan", String.class, "unknown")
                                        ),
                                Defaults.getColumnValueOrDefault(row, "cnt", Long.class, 0L)))
                .all()
                .defaultIfEmpty(Tuples.of("unknown", 0L));
    }

    protected Mono<Long> countByDateRange(LocalDate start, LocalDate end) {
        Criteria criteria =
                Criteria.where("last_updated").lessThanOrEquals(LocalDateTime.of(end, LocalTime.MAX)).and("last_updated").greaterThan(LocalDateTime.of(start, LocalTime.MIDNIGHT));
        return
                client
                .select(ServiceInstanceDetail.class)
                .matching(Query.query(criteria))
                .all()
                .count()
                .defaultIfEmpty(0L);
    }

    protected Mono<Long> countStagnant(LocalDate end) {
        Criteria criteria =
                Criteria.where("last_updated").lessThan(LocalDateTime.of(end, LocalTime.MIDNIGHT));
        return
                client
                .select(ServiceInstanceDetail.class)
                .matching(Query.query(criteria))
                .all()
                .count()
                .defaultIfEmpty(0L);
    }

    public Mono<Long> totalServiceInstances() {
        String sql = "select count(*) as cnt from service_instance_detail";
        return
                client
                .getDatabaseClient()
                .sql(sql)
                .map((row, metadata) -> Defaults.getColumnValueOrDefault(row, "cnt", Long.class, 0L))
                .one()
                .defaultIfEmpty(0L);
    }

    public Flux<Tuple2<String, Long>> totalVelocity() {
        final LocalDate now = LocalDate.now();
        return
                Flux.concat(
                        countByDateRange(now.minusDays(1), now).map(r -> Tuples.of("in-last-day", r)),
                        countByDateRange(now.minusDays(2), now.minusDays(1)).map(r -> Tuples.of("between-one-day-and-two-days", r)),
                        countByDateRange(now.minusWeeks(1), now.minusDays(2)).map(r -> Tuples.of("between-two-days-and-one-week", r)),
                        countByDateRange(now.minusWeeks(2), now.minusWeeks(1)).map(r -> Tuples.of("between-one-week-and-two-weeks", r)),
                        countByDateRange(now.minusMonths(1), now.minusWeeks(2)).map(r -> Tuples.of("between-two-weeks-and-one-month", r)),
                        countByDateRange(now.minusMonths(3), now.minusMonths(1)).map(r -> Tuples.of("between-one-month-and-three-months", r)),
                        countByDateRange(now.minusMonths(6), now.minusMonths(3)).map(r -> Tuples.of("between-three-months-and-six-months", r)),
                        countByDateRange(now.minusYears(1), now.minusMonths(6)).map(r -> Tuples.of("between-six-months-and-one-year", r)),
                        countStagnant(now.minusYears(1)).map(r -> Tuples.of("beyond-one-year", r)));
    }

}
