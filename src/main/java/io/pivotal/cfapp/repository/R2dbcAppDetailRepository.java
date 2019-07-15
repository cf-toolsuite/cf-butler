package io.pivotal.cfapp.repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.DatabaseClient.GenericInsertSpec;
import org.springframework.data.r2dbc.query.Criteria;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.AppDetail;
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
	public R2dbcAppDetailRepository(DatabaseClient client) {
		this.client = client;
	}

	public Mono<AppDetail> save(AppDetail entity) {
		GenericInsertSpec<Map<String, Object>> spec =
			client.insert().into(AppDetail.tableName())
				.value("organization", entity.getOrganization());
		spec = spec.value("space", entity.getSpace());
		spec = spec.value("app_id", entity.getAppId());
		spec = spec.value("app_name", entity.getAppName());
		if (entity.getBuildpack() != null) {
			spec = spec.value("buildpack", entity.getBuildpack());
		} else {
			spec = spec.nullValue("buildpack");
		}
		if (entity.getImage() != null) {
			spec = spec.value("image", entity.getImage());
		} else {
			spec = spec.nullValue("image");
		}
		if (entity.getStack() != null) {
			spec = spec.value("stack", entity.getStack());
		} else {
			spec = spec.nullValue("stack");
		}
		if (entity.getRunningInstances() != null) {
			spec = spec.value("running_instances",entity.getRunningInstances());
		} else {
			spec = spec.nullValue("running_instances");
		}
		if (entity.getTotalInstances() != null) {
			spec = spec.value("total_instances", entity.getTotalInstances());
		} else {
			spec = spec.nullValue("total_instances");
		}
		if (entity.getMemoryUsage() != null) {
			spec = spec.value("memory_used", entity.getMemoryUsage());
		} else {
			spec = spec.nullValue("memory_used");
		}
		if (entity.getDiskUsage() != null) {
			spec = spec.value("disk_used", entity.getDiskUsage());
		} else {
			spec = spec.nullValue("disk_used");
		}
		if (entity.getUrls() != null) {
			spec = spec.value("urls", String.join(",", entity.getUrls()));
		} else {
			spec = spec.nullValue("urls");
		}
		if (entity.getLastPushed() != null) {
			spec = spec.value("last_pushed", entity.getLastPushed());
		} else {
			spec = spec.nullValue("last_pushed");
		}
		if (entity.getLastEvent() != null) {
			spec = spec.value("last_event", entity.getLastEvent());
		} else {
			spec = spec.nullValue("last_event");
		}
		if (entity.getLastEventActor() != null) {
			spec = spec.value("last_event_actor", entity.getLastEventActor());
		} else {
			spec = spec.nullValue("last_event_actor");
		}
		if (entity.getLastEventTime() != null) {
			spec = spec.value("last_event_time", entity.getLastEventTime());
		} else {
			spec = spec.nullValue("last_event_time");
		}
		if (entity.getRequestedState() != null) {
			spec = spec.value("requested_state", entity.getRequestedState());
		} else {
			spec = spec.nullValue("requested_state");
		}
		return spec.fetch().rowsUpdated().then(Mono.just(entity));
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
		AppDetail partial =
			AppDetail
				.builder()
					.pk(row.get("pk", Long.class))
					.organization(Defaults.getValueOrDefault(row.get("organization", String.class), ""))
					.space(Defaults.getValueOrDefault(row.get("space", String.class), ""))
					.appId(Defaults.getValueOrDefault(row.get("app_id", String.class), ""))
					.appName(Defaults.getValueOrDefault(row.get("app_name", String.class), ""))
					.buildpack(Defaults.getValueOrDefault(row.get("buildpack", String.class), ""))
					.runningInstances(Defaults.getValueOrDefault(row.get("running_instances", Integer.class), 0))
					.totalInstances(Defaults.getValueOrDefault(row.get("total_instances", Integer.class), 0))
					.memoryUsage(Defaults.getValueOrDefault(row.get("memory_used", Long.class), 0L))
					.diskUsage(Defaults.getValueOrDefault(row.get("disk_used", Long.class), 0L))
					.image(Defaults.getValueOrDefault(row.get("image", String.class), ""))
					.stack(Defaults.getValueOrDefault(row.get("stack", String.class), ""))
					.urls(Arrays.asList(Defaults.getValueOrDefault(row.get("urls", String.class), "").split("\\s*,\\s*")))
					.lastEvent(Defaults.getValueOrDefault(row.get("last_event", String.class), ""))
					.lastEventActor(Defaults.getValueOrDefault(row.get("last_event_actor", String.class), ""))
					.requestedState(Defaults.getValueOrDefault(row.get("requested_state", String.class), ""))
					.build();
		// FIXME Dirty hack! We can remove this bit of code when https://github.com/r2dbc/r2dbc-h2/issues/78 is addressed.
		AppDetail partialWithEventTime = null;
		try {
			partialWithEventTime = AppDetail.from(partial).lastEventTime(row.get("last_event_time", LocalDateTime.class)).build();
		} catch (ClassCastException cce) {
			partialWithEventTime = partial;
		}
		try {
			return AppDetail.from(partialWithEventTime).lastPushed(row.get("last_pushed", LocalDateTime.class)).build();
		} catch (ClassCastException cce) {
			return partialWithEventTime;
		}
	}

	private Tuple2<AppDetail, ApplicationPolicy> toTuple(AppDetail detail, ApplicationPolicy policy) {
		return Tuples.of(detail, policy);
	}

}
