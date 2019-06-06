package io.pivotal.cfapp.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.function.DatabaseClient.GenericInsertSpec;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.config.DbmsSettings;
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
	private final DbmsSettings settings;

	@Autowired
	public R2dbcAppDetailRepository(
		DatabaseClient client,
		DbmsSettings settings) {
		this.client = client;
		this.settings = settings;
	}

	public Mono<AppDetail> save(AppDetail entity) {
		GenericInsertSpec<Map<String, Object>> spec =
			client.insert().into("application_detail")
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
		String select = "select pk, organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, memory_used, disk_used, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state from application_detail order by organization, space, app_name";
		return client.execute().sql(select)
						.map((row, metadata) -> fromRow(row))
						.all();
	}

	public Mono<Void> deleteAll() {
		return client.execute().sql("delete from application_detail")
						.fetch()
						.rowsUpdated()
						.then();
	}

	public Flux<AppDetail> findByDateRange(LocalDate start, LocalDate end) {
		String sql = "select pk, organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, memory_used, disk_used, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state from application_detail where last_pushed <= " + settings.getBindPrefix() + 2 + " and last_pushed > " + settings.getBindPrefix() + 1 + " order by last_pushed desc";
		return client.execute().sql(sql)
				.bind(settings.getBindPrefix() + 1, LocalDateTime.of(end, LocalTime.MAX))
				.bind(settings.getBindPrefix() + 2, LocalDateTime.of(start, LocalTime.MIDNIGHT))
				.map((row, metadata) -> fromRow(row))
				.all();
	}

	public Mono<AppDetail> findByAppId(String appId) {
		String index = settings.getBindPrefix() + 1;
		String selectOne = "select pk, organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, memory_used, disk_used, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state from application_detail where app_id = " + index;
		return client.execute().sql(selectOne)
						.bind(index, appId)
						.map((row, metadata) -> fromRow(row))
						.one();
	}

	public Flux<Tuple2<AppDetail, ApplicationPolicy>> findByApplicationPolicy(ApplicationPolicy policy, boolean mayHaveServiceBindings) {
		return mayHaveServiceBindings == true
				? findApplicationsThatMayHaveServiceBindings(policy)
						: findApplicationsThatDoNotHaveServiceBindings(policy);
	}

	private Flux<Tuple2<AppDetail, ApplicationPolicy>> findApplicationsThatMayHaveServiceBindings(ApplicationPolicy policy) {
		String select = "select pk, organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, memory_used, disk_used, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state";
		String from = "from application_detail";
		StringBuilder where = new StringBuilder();
		where.append("where requested_state = " + settings.getBindPrefix() + 1 + " ");
		LocalDateTime temporal = null;
		if (policy.getFromDateTime() != null) {
			where.append("and last_event_time <= " + settings.getBindPrefix() + 2 + " ");
			temporal = policy.getFromDateTime();
		}
		if (policy.getFromDuration() != null) {
			where.append("and last_event_time <= " + settings.getBindPrefix() + 2 + " ");
			LocalDateTime eventTime = LocalDateTime.now().minus(policy.getFromDuration());
			temporal = eventTime;
		}
		String orderBy = "order by organization, space, app_name";
		String sql = String.join(" ", select, from, where, orderBy);
		return client.execute().sql(sql)
						.bind(settings.getBindPrefix() + 1, policy.getState())
						.bind(settings.getBindPrefix() + 2, temporal)
						.map((row, metadata) -> fromRow(row))
						.all()
						.map(r -> toTuple(r, policy));
	}

	private Flux<Tuple2<AppDetail, ApplicationPolicy>> findApplicationsThatDoNotHaveServiceBindings(ApplicationPolicy policy) {
		String select =
				"select ad.pk, ad.organization, ad.space, ad.app_id, ad.app_name, ad.buildpack, ad.image, " +
				"ad.stack, ad.running_instances, ad.total_instances, ad.memory_used, ad.disk_used, ad.urls, ad.last_pushed, ad.last_event, " +
				"ad.last_event_actor, ad.last_event_time, ad.requested_state";
		String from = "from application_detail ad";
		String leftJoin = "left join application_relationship ar on ad.app_id = ar.app_id";
		StringBuilder where = new StringBuilder();
		where.append("where ar.service_instance_id is null and ad.requested_state = " + settings.getBindPrefix() + 1 + " ");
		LocalDateTime temporal = null;
		if (policy.getFromDateTime() != null) {
			where.append("and ad.last_event_time <= " + settings.getBindPrefix() + 2 + " ");
			temporal = policy.getFromDateTime();
		}
		if (policy.getFromDuration() != null) {
			where.append("and ad.last_event_time <= " + settings.getBindPrefix() + 2 + " ");
			LocalDateTime eventTime = LocalDateTime.now().minus(policy.getFromDuration());
			temporal = eventTime;
		}
		String orderBy = "order by ad.organization, ad.space, ad.app_name";
		String sql = String.join(" ", select, from, leftJoin, where, orderBy);
		return client.execute().sql(sql)
						.bind(settings.getBindPrefix() + 1, policy.getState())
						.bind(settings.getBindPrefix() + 2, temporal)
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
