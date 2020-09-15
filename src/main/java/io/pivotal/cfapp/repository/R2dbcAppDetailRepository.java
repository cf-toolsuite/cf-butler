package io.pivotal.cfapp.repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppDetailShim;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import io.pivotal.cfapp.domain.Defaults;
import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Repository
public class R2dbcAppDetailRepository {

	private final DatabaseClient client;

	@Autowired
	public R2dbcAppDetailRepository(R2dbcEntityOperations ops) {
		this.client = DatabaseClient.create(ops.getDatabaseClient().getConnectionFactory());
	}

	public Mono<AppDetail> save(AppDetail entity) {
		AppDetailShim shim = AppDetailShim.from(entity);
		return
			client
				.insert()
				.into(AppDetailShim.class)
				.table(AppDetail.tableName())
				.using(shim)
				.fetch()
				.rowsUpdated()
				.then(Mono.just(entity));
	}

	public Flux<AppDetail> findAll() {
		return client
				.select()
				.from(AppDetail.tableName())
				.project(AppDetail.columnNames())
				.orderBy(Order.asc("organization"), Order.asc("space"), Order.asc("app_name"))
				.map((row, metadata) -> fromRow(row))
				.all();
	}

	public Mono<Void> deleteAll() {
		return client
				.delete()
				.from(AppDetail.tableName())
				.fetch()
				.rowsUpdated()
				.then();
	}

	public Flux<AppDetail> findByDateRange(LocalDate start, LocalDate end) {
		return client
				.select()
					.from(AppDetail.tableName())
					.project(AppDetail.columnNames())
					.matching(Criteria.where("last_pushed").lessThanOrEquals(LocalDateTime.of(end, LocalTime.MAX)).and("last_pushed").greaterThan(LocalDateTime.of(start, LocalTime.MIDNIGHT)))
					.orderBy(Order.desc("last_pushed"))
				.map((row, metadata) -> fromRow(row))
				.all();
	}

	public Mono<AppDetail> findByAppId(String appId) {
		return client
				.select()
					.from(AppDetail.tableName())
					.project(AppDetail.columnNames())
					.matching(Criteria.where("app_id").is(appId))
				.map((row, metadata) -> fromRow(row))
				.one();
	}

	public Flux<Tuple2<AppDetail, ApplicationPolicy>> findByApplicationPolicy(ApplicationPolicy policy, boolean mayHaveServiceBindings) {
		return mayHaveServiceBindings == true
				? findApplicationsThatMayHaveServiceBindings(policy)
						: findApplicationsThatDoNotHaveServiceBindings(policy);
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
			temporal = LocalDateTime.now().minus(Duration.parse(fromDuration));;
		}
		if (temporal != null) {
			criteria = Criteria.where("requested_state").is(policy.getState()).and("last_event_time").lessThanOrEquals(temporal);
		} else {
			criteria = Criteria.where("requested_state").is(policy.getState());
		}
		return
			client
				.select()
					.from(AppDetail.tableName())
					.project(AppDetail.columnNames())
					.matching(criteria)
					.orderBy(Order.asc("organization"), Order.asc("space"), Order.asc("app_name"))
				.map((row, metadata) -> fromRow(row))
						.all()
						.map(r -> toTuple(r, policy));
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
			temporal = LocalDateTime.now().minus(Duration.parse(fromDuration));;
		}
		if (temporal != null) {
			criteria = Criteria.where("requested_state").is(policy.getState()).and("service_instance_id").isNull().and("last_event_time").lessThanOrEquals(temporal);
		} else {
			criteria = Criteria.where("requested_state").is(policy.getState()).and("service_instance_id").isNull();
		}
		return
			client
				.select()
					.from("service_bindings")
					.project(AppDetail.columnNames())
					.matching(criteria)
					.orderBy(Order.asc("organization"), Order.asc("space"), Order.asc("app_name"))
				.map((row, metadata) -> fromRow(row))
						.all()
						.map(r -> toTuple(r, policy));
	}

	private AppDetail fromRow(Row row) {
		return
			AppDetail
				.builder()
					.pk(row.get("pk", Long.class))
					.organization(Defaults.getColumnValue(row, "organization", String.class))
					.space(Defaults.getColumnValue(row, "space", String.class))
					.appId(Defaults.getColumnValue(row, "app_id", String.class))
					.appName(Defaults.getColumnValue(row, "app_name", String.class))
					.buildpack(Defaults.getColumnValue(row, "buildpack", String.class))
					.buildpackVersion(Defaults.getColumnValue(row, "buildpack_version", String.class))
					.runningInstances(Defaults.getColumnValueOrDefault(row, "running_instances", Integer.class, 0))
					.totalInstances(Defaults.getColumnValueOrDefault(row, "total_instances", Integer.class, 0))
					.memoryUsed(Defaults.getColumnValueOrDefault(row, "memory_used", Long.class, 0L))
					.diskUsed(Defaults.getColumnValueOrDefault(row, "disk_used", Long.class, 0L))
					.image(Defaults.getColumnValue(row, "image", String.class))
					.stack(Defaults.getColumnValue(row, "stack", String.class))
					.urls(Defaults.getColumnListOfStringValue(row, "urls"))
					.lastPushed(Defaults.getColumnValue(row, "last_pushed", LocalDateTime.class))
					.lastEventTime(Defaults.getColumnValue(row, "last_event_time", LocalDateTime.class))
					.lastEvent(Defaults.getColumnValue(row, "last_event", String.class))
					.lastEventActor(Defaults.getColumnValue(row, "last_event_actor", String.class))
					.requestedState(Defaults.getColumnValue(row, "requested_state", String.class))
					.buildpackReleaseType(Defaults.getColumnValue(row, "buildpack_release_type", String.class))
					.buildpackReleaseDate(Defaults.getColumnValue(row, "buildpack_release_date", LocalDateTime.class))
					.buildpackLatestVersion(Defaults.getColumnValue(row, "buildpack_latest_version", String.class))
					.buildpackLatestUrl(Defaults.getColumnValue(row, "buildpack_latest_url", String.class))
					.build();
	}

	private Tuple2<AppDetail, ApplicationPolicy> toTuple(AppDetail detail, ApplicationPolicy policy) {
		return Tuples.of(detail, policy);
	}

}
