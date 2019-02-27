package io.pivotal.cfapp.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.ApplicationPolicy;
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
		return client.insert().into("application_detail")
						.value("organization", entity.getOrganization())
						.value("space", entity.getSpace())
						.value("app_id", entity.getAppId())
						.value("app_name", entity.getAppName())
						.value("buildpack", entity.getBuildpack())
						.value("image", entity.getImage())
						.value("stack", entity.getStack())
						.value("running_instances", entity.getRunningInstances())
						.value("total_instances", entity.getTotalInstances())
						.value("urls", entity.getUrls())
						.value("last_pushed", entity.getLastPushed() != null ? Timestamp.valueOf(entity.getLastPushed()): null)
						.value("last_event", entity.getLastEvent())
						.value("last_event_actor", entity.getLastEventActor())
						.value("last_event_time", entity.getLastEventTime() != null ? Timestamp.valueOf(entity.getLastEventTime()): null)
						.value("requested_state", entity.getRequestedState())
						.fetch()
						.rowsUpdated()
						.then(Mono.just(entity));
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
		String selectOne = "select organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state from application_detail where app_id = ?";
		return client.execute().sql(selectOne)
						.bind("app_id", appId)
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
		String select = "select organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state";
		String from = "from application_detail";
		StringBuilder where = new StringBuilder();
		List<Object> paramValues = new ArrayList<>();
		where.append("where requested_state = ? ");
		paramValues.add(policy.getState());
		if (policy.getFromDateTime() != null) {
			where.append("and last_event_time <= ? ");
			paramValues.add(Timestamp.valueOf(policy.getFromDateTime()));
		}
		if (policy.getFromDuration() != null) {
			where.append("and last_event_time <= ?");
			LocalDateTime eventTime = LocalDateTime.now().minus(policy.getFromDuration());
			paramValues.add(Timestamp.valueOf(eventTime));
		}
		String orderBy = "order by organization, space, app_name";
		String sql = String.join(" ", select, from, where, orderBy);
		return client.execute().sql(sql).as(AppDetail.class)
						.fetch().all().map(r -> toTuple(r, policy));
	}

	private Flux<Tuple2<AppDetail, ApplicationPolicy>> findApplicationsThatDoNotHaveServiceBindings(ApplicationPolicy policy) {
		String select =
				"select ad.organization, ad.space, ad.app_id, ad.app_name, ad.buildpack, ad.image, " + 
				"ad.stack, ad.running_instances, ad.total_instances, ad.urls, ad.last_pushed, ad.last_event, " + 
				"ad.last_event_actor, ad.last_event_time, ad.requested_state";
		String from = "from application_detail ad";
		String leftJoin = "left join application_relationship ar on ad.app_id = ar.app_id";
		StringBuilder where = new StringBuilder();
		List<Object> paramValues = new ArrayList<>();
		where.append("where ar.service_id is null and ad.requested_state = ? ");
		paramValues.add(policy.getState());
		if (policy.getFromDateTime() != null) {
			where.append("and ad.last_event_time <= ?");
			paramValues.add(Timestamp.valueOf(policy.getFromDateTime()));
		}
		if (policy.getFromDuration() != null) {
			where.append("and ad.last_event_time <= ?");
			LocalDateTime eventTime = LocalDateTime.now().minus(policy.getFromDuration());
			paramValues.add(Timestamp.valueOf(eventTime));
		}
		String orderBy = "order by ad.organization, ad.space, ad.app_name";
		String sql = String.join(" ", select, from, leftJoin, where, orderBy);
		return client.execute().sql(sql).as(AppDetail.class)
						.fetch().all().map(r -> toTuple(r, policy));
	}

	private Tuple2<AppDetail, ApplicationPolicy> toTuple(AppDetail detail, ApplicationPolicy policy) {
		return Tuples.of(detail, policy);
	}

}
