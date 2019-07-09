package io.pivotal.cfapp.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.query.Criteria;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.config.DbmsSettings;
import io.pivotal.cfapp.domain.Defaults;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Repository
public class R2dbcAppMetricsRepository {

	private final DatabaseClient client;
	private final DbmsSettings settings;

	@Autowired
	public R2dbcAppMetricsRepository(
		DatabaseClient client,
		DbmsSettings settings) {
		this.client = client;
		this.settings = settings;
	}

	protected Flux<Tuple2<String, Long>> by(String columnName) {
		String sql = "select " + columnName + ", count(" + columnName + ") as cnt from application_detail group by " + columnName;
		return client.execute(sql)
					.map((row, metadata)
							-> Tuples.of(Defaults.getValueOrDefault(row.get(columnName, String.class), "--"), Defaults.getValueOrDefault(row.get("cnt", Long.class), 0L)))
					.all()
					.defaultIfEmpty(Tuples.of("--", 0L));
	}

	protected Mono<Long> countByDateRange(LocalDate start, LocalDate end) {
		return client
				.select()
					.from("application_detail")
					.project("last_pushed")
					.matching(Criteria.where("last_pushed").lessThanOrEquals(LocalDateTime.of(end, LocalTime.MAX)).and("last_pushed").greaterThan(LocalDateTime.of(start, LocalTime.MIDNIGHT)))
				.map((row, metadata) -> Defaults.getValueOrDefault(row.get("last_pushed", LocalDateTime.class), 0L))
				.all()
				.count()
				.defaultIfEmpty(0L);
	}

	protected Mono<Long> countStagnant(LocalDate end) {
		return client
				.select()
					.from("application_detail")
					.project("last_pushed")
					.matching(Criteria.where("last_pushed").lessThan(LocalDateTime.of(end, LocalTime.MIDNIGHT)))
				.map((row, metadata) -> Defaults.getValueOrDefault(row.get("last_pushed", LocalDateTime.class), 0L))
				.all()
				.count()
				.defaultIfEmpty(0L);
	}

	public Flux<Tuple2<String, Long>> byOrganization() {
		return by("organization");
	}

	public Flux<Tuple2<String, Long>> byStack() {
		return by("stack");
	}

	public Flux<Tuple2<String, Long>> byBuildpack() {
		String sql = "select buildpack, count(buildpack) as cnt from application_detail where image is null and buildpack is not null group by buildpack";
		return client.execute(sql)
					.map((row, metadata)
							-> Tuples.of(Defaults.getValueOrDefault(row.get("buildpack", String.class), "--"), Defaults.getValueOrDefault(row.get("cnt", Long.class), 0L)))
					.all()
					.defaultIfEmpty(Tuples.of("--", 0L));
	}

	public Flux<Tuple2<String, Long>> byDockerImage() {
		String sql = "select image, count(image) as cnt from application_detail where image is not null group by image";
		return client.execute(sql)
					.map((row, metadata)
							-> Tuples.of(Defaults.getValueOrDefault(row.get("image", String.class), "--"), Defaults.getValueOrDefault(row.get("cnt", Long.class), 0L)))
					.all()
					.defaultIfEmpty(Tuples.of("--", 0L));
	}

	public Flux<Tuple2<String, Long>> byStatus() {
		return by("requested_state");
	}

	public Mono<Long> totalApplications() {
		String sql = "select count(*) as cnt from application_detail";
		return client.execute(sql)
				.map((row, metadata) -> Defaults.getValueOrDefault(row.get("cnt", Long.class), 0L))
				.one()
				.defaultIfEmpty(0L);
	}

	public Mono<Long> totalApplicationInstances() {
		String sql = "select sum(total_instances) as cnt from application_detail";
		if (settings.getProvider().equals("MySQL")) {
			sql = "select cast(sum(total_instances) as signed) as cnt from application_detail";
		}
		return client.execute(sql)
				.map((row, metadata) -> Defaults.getValueOrDefault(row.get("cnt", Long.class), 0L))
				.one()
				.defaultIfEmpty(0L);
	}

	public Mono<Long> totalRunningApplicationInstances() {
		String sql = "select sum(running_instances) as cnt from application_detail where requested_state = 'started'";
		if (settings.getProvider().equals("MySQL")) {
			sql = "select cast(sum(running_instances) as signed) as cnt from application_detail where requested_state = 'started'";
		}
		return client.execute(sql)
				.map((row, metadata) -> Defaults.getValueOrDefault(row.get("cnt", Long.class), 0L))
				.one()
				.defaultIfEmpty(0L);
	}

	public Mono<Long> totalCrashedApplicationInstances() {
		String sql = "select count(running_instances) as cnt from application_detail where requested_state = 'started' and running_instances = 0";
		return client.execute(sql)
				.map((row, metadata) -> Defaults.getValueOrDefault(row.get("cnt", Long.class), 0L))
				.one()
				.defaultIfEmpty(0L);
	}

	public Mono<Long> totalStoppedApplicationInstances() {
		String sql = "select sum(total_instances) as cnt from application_detail where requested_state = 'stopped'";
		if (settings.getProvider().equals("MySQL")) {
			sql = "select cast(sum(total_instances) as signed) as cnt from application_detail where requested_state = 'stopped'";
		}
		return client.execute(sql)
				.map((row, metadata) -> Defaults.getValueOrDefault(row.get("cnt", Long.class), 0L))
				.one()
				.defaultIfEmpty(0L);
	}

	public Mono<Double> totalMemoryUsed() {
		String sql = "select sum(memory_used) as tot from application_detail";
		return client.execute(sql)
				.map((row, metadata) -> Defaults.getValueOrDefault(row.get("tot", BigDecimal.class), BigDecimal.valueOf(0L)))
				.one()
				.map(r -> toGigabytes(r))
				.defaultIfEmpty(0.0);
	}

	public Mono<Double> totalDiskUsed() {
		String sql = "select sum(disk_used) as tot from application_detail";
		return client.execute(sql)
				.map((row, metadata) -> Defaults.getValueOrDefault(row.get("tot", BigDecimal.class), BigDecimal.valueOf(0L)))
				.one()
				.map(r -> toGigabytes(r))
				.defaultIfEmpty(0.0);
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

	private Double toGigabytes(BigDecimal input) {
		return Double.valueOf(input.doubleValue() / 1000000000.0);
	}

}
