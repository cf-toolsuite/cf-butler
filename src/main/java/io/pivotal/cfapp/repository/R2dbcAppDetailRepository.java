package io.pivotal.cfapp.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.function.DatabaseClient.GenericInsertSpec;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.config.DbmsSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.ApplicationPolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Repository
public class R2dbcAppDetailRepository {

	private final DatabaseClient client;
	private DbmsSettings settings;

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
			spec = spec.nullValue("buildpack", String.class);
		}
		if (entity.getImage() != null) {
			spec = spec.value("image", entity.getImage());
		} else {
			spec = spec.nullValue("image", String.class);
		}
		if (entity.getStack() != null) {
			spec = spec.value("stack", entity.getStack());
		} else {
			spec = spec.nullValue("stack", String.class);
		}
		if (entity.getRunningInstances() != null) {
			spec = spec.value("running_instances",entity.getRunningInstances());
		} else {
			spec = spec.nullValue("running_instances", String.class);
		}
		if (entity.getTotalInstances() != null) {
			spec = spec.value("total_instances", entity.getTotalInstances());
		} else {
			spec = spec.nullValue("total_instances", String.class);
		}
		if (entity.getUrls() != null) {
			spec = spec.value("urls", entity.getUrls());
		} else {
			spec = spec.nullValue("urls", String.class);
		}
		if (entity.getLastPushed() != null) {
			spec = spec.value("last_pushed", Timestamp.valueOf(entity.getLastPushed()));
		} else {
			spec = spec.nullValue("last_pushed", Timestamp.class);
		}
		if (entity.getLastEvent() != null) {
			spec = spec.value("last_event", entity.getLastEvent());
		} else {
			spec = spec.nullValue("last_event", String.class);
		}
		if (entity.getLastEventActor() != null) {
			spec = spec.value("last_event_actor", entity.getLastEventActor());
		} else {
			spec = spec.nullValue("last_event_actor", String.class);
		}
		if (entity.getLastEventTime() != null) {
			spec = spec.value("last_event_time", Timestamp.valueOf(entity.getLastEventTime()));
		} else {
			spec = spec.nullValue("last_event_time", Timestamp.class);
		}
		if (entity.getRequestedState() != null) {
			spec = spec.value("requested_state", entity.getRequestedState());
		} else {
			spec = spec.nullValue("requested_state", String.class);
		}
		return spec.fetch().rowsUpdated().then(Mono.just(entity));
	}

	public Flux<AppDetail> findAll() {
		return client.select().from("application_detail")
						.orderBy(Sort.by("organization", "space", "app_name"))
						.as(AppDetail.class)
						.fetch()
						.all();
	}

	public Mono<Void> deleteAll() {
		return client.execute().sql("delete from application_detail")
						.fetch()
						.rowsUpdated()
						.then();
	}

	public Mono<AppDetail> findByAppId(String appId) {
		String index = settings.getBindPrefix() + 1;
		String selectOne = "select pk, organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state from application_detail where app_id = " + index;
		return client.execute().sql(selectOne)
						.bind(index, appId)
						.as(AppDetail.class)
						.fetch()
						.one();
	}

	public Flux<Tuple2<AppDetail, ApplicationPolicy>> findByApplicationPolicy(ApplicationPolicy policy, boolean mayHaveServiceBindings) {
		return mayHaveServiceBindings == true
				? findApplicationsThatMayHaveServiceBindings(policy)
						: findApplicationsThatDoNotHaveServiceBindings(policy);
	}

	private Flux<Tuple2<AppDetail, ApplicationPolicy>> findApplicationsThatMayHaveServiceBindings(ApplicationPolicy policy) {
		String select = "select pk, organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state";
		String from = "from application_detail";
		StringBuilder where = new StringBuilder();
		where.append("where requested_state = " + settings.getBindPrefix() + 1 + " ");
		Timestamp temporal = null;
		if (policy.getFromDateTime() != null) {
			where.append("and last_event_time <= " + settings.getBindPrefix() + 2 + " ");
			temporal = Timestamp.valueOf(policy.getFromDateTime());
		}
		if (policy.getFromDuration() != null) {
			where.append("and last_event_time <= " + settings.getBindPrefix() + 2 + " ");
			LocalDateTime eventTime = LocalDateTime.now().minus(policy.getFromDuration());
			temporal = Timestamp.valueOf(eventTime);
		}
		String orderBy = "order by organization, space, app_name";
		String sql = String.join(" ", select, from, where, orderBy);
		return client.execute().sql(sql)
						.bind(settings.getBindPrefix() + 1, policy.getState())
						.bind(settings.getBindPrefix() + 2, temporal)
						.as(AppDetail.class)
						.fetch().all().map(r -> toTuple(r, policy));
	}

	private Flux<Tuple2<AppDetail, ApplicationPolicy>> findApplicationsThatDoNotHaveServiceBindings(ApplicationPolicy policy) {
		String select =
				"select ad.pk, ad.organization, ad.space, ad.app_id, ad.app_name, ad.buildpack, ad.image, " + 
				"ad.stack, ad.running_instances, ad.total_instances, ad.urls, ad.last_pushed, ad.last_event, " + 
				"ad.last_event_actor, ad.last_event_time, ad.requested_state";
		String from = "from application_detail ad";
		String leftJoin = "left join application_relationship ar on ad.app_id = ar.app_id";
		StringBuilder where = new StringBuilder();
		where.append("where ar.service_id is null and ad.requested_state = " + settings.getBindPrefix() + 1 + " ");
		Timestamp temporal = null;
		if (policy.getFromDateTime() != null) {
			where.append("and ad.last_event_time <= " + settings.getBindPrefix() + 2 + " ");
			temporal = Timestamp.valueOf(policy.getFromDateTime());
		}
		if (policy.getFromDuration() != null) {
			where.append("and ad.last_event_time <= " + settings.getBindPrefix() + 2 + " ");
			LocalDateTime eventTime = LocalDateTime.now().minus(policy.getFromDuration());
			temporal = Timestamp.valueOf(eventTime);
		}
		String orderBy = "order by ad.organization, ad.space, ad.app_name";
		String sql = String.join(" ", select, from, leftJoin, where, orderBy);
		return client.execute().sql(sql)
						.bind(settings.getBindPrefix() + 1, policy.getState())
						.bind(settings.getBindPrefix() + 2, temporal)
						.as(AppDetail.class)
						.fetch().all().map(r -> toTuple(r, policy));
	}

	private Tuple2<AppDetail, ApplicationPolicy> toTuple(AppDetail detail, ApplicationPolicy policy) {
		return Tuples.of(detail, policy);
	}

}
