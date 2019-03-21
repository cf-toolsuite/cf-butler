package io.pivotal.cfapp.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.config.DbmsSettings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Repository
public class R2dbcServiceInstanceMetricsRepository {

	private final DatabaseClient client;
	private final DbmsSettings settings;

	@Autowired
	public R2dbcServiceInstanceMetricsRepository(
		DatabaseClient client,
		DbmsSettings settings) {
		this.client = client;
		this.settings = settings;
	}

	protected Flux<Tuple2<String, Long>> by(String columnName) {
		String sql = "select " + columnName + ", count(" + columnName + ") as cnt from service_instance_detail group by " + columnName;
		return client.execute().sql(sql)
					.map((row, metadata)
							-> Tuples.of(row.get(columnName, String.class) != null ? row.get(columnName, String.class): "--", row.get("cnt", Long.class)))
					.all()
					.defaultIfEmpty(Tuples.of("--", 0L));
	}

	protected Mono<Long> countByDateRange(LocalDate start, LocalDate end) {
		String sql = "select count(last_updated) as cnt from service_instance_detail where last_updated <= " + settings.getBindPrefix() + 1 + " and last_updated > " + settings.getBindPrefix() + 2;
		return client.execute().sql(sql)
				.bind(settings.getBindPrefix() + 1, Timestamp.valueOf(LocalDateTime.of(end, LocalTime.MAX)))
				.bind(settings.getBindPrefix() + 2, Timestamp.valueOf(LocalDateTime.of(start, LocalTime.MIDNIGHT)))
				.map((row, metadata) -> row.get("cnt", Long.class))
				.one()
				.defaultIfEmpty(0L);
	}

	protected Mono<Long> countStagnant(LocalDate end) {
		String sql = "select count(last_updated) as cnt from service_instance_detail where last_updated < " + settings.getBindPrefix() + 1;
		return client.execute().sql(sql)
				.bind(settings.getBindPrefix() + 1, Timestamp.valueOf(LocalDateTime.of(end, LocalTime.MIDNIGHT)))
				.map((row, metadata) -> row.get("cnt", Long.class))
				.one()
				.defaultIfEmpty(0L);
	}

	public Flux<Tuple2<String, Long>> byOrganization() {
		return by("organization");
	}

	public Flux<Tuple2<String, Long>> byService() {
		String sqlup = "select type, count(type) as cnt from service_instance_detail where type = 'user_provided_service_instance' group by service";
		Flux<Tuple2<String, Long>> ups = client.execute().sql(sqlup)
					.map((row, metadata)
							-> Tuples.of("user-provided", row.get("cnt", Long.class)))
					.all()
					.defaultIfEmpty(Tuples.of("user-provided", 0L));
		String sqlms = "select service, count(service) as cnt from service_instance_detail where type = 'managed_service_instance' group by service";
		Flux<Tuple2<String, Long>> ms = client.execute().sql(sqlms)
					.map((row, metadata)
							-> Tuples.of(row.get("service", String.class) != null ? row.get("service", String.class): "managed", row.get("cnt", Long.class)))
					.all()
					.defaultIfEmpty(Tuples.of("managed", 0L));
		return ups.concatWith(ms);
	}

	public Flux<Tuple2<String, Long>> byServiceAndPlan() {
		String sql = "select service, plan, count(*) as cnt from service_instance_detail where type = 'managed_service_instance' group by service, plan";
		return client.execute().sql(sql)
					.map((row, metadata)
							-> Tuples.of(
									String.join(
										"/",
										row.get("service", String.class) != null ? row.get("service", String.class): "unknown",
										row.get("plan", String.class) != null ? row.get("plan", String.class): "unknown"
									),
									row.get("cnt", Long.class)))
					.all()
					.defaultIfEmpty(Tuples.of("unknown", 0L));
	}

	public Mono<Long> totalServiceInstances() {
		String sql = "select count(*) as cnt from service_instance_detail";
		return client.execute().sql(sql)
				.map((row, metadata) -> row.get("cnt", Long.class))
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
