package io.pivotal.cfapp.repository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.DatabaseClient.GenericInsertSpec;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.HistoricalRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcHistoricalRecordRepository {

	private final DatabaseClient client;

	@Autowired
	public R2dbcHistoricalRecordRepository(DatabaseClient client) {
		this.client = client;
	}

	public Mono<HistoricalRecord> save(HistoricalRecord entity) {
		GenericInsertSpec<Map<String, Object>> spec =
			client.insert().into("historical_record")
				.value("transaction_datetime", entity.getTransactionDateTime());
		spec = spec.value("action_taken", entity.getActionTaken());
		spec = spec.value("organization", entity.getOrganization());
		spec = spec.value("space", entity.getSpace());
		if (entity.getAppId() != null) {
			spec = spec.value("app_id", entity.getAppId());
		} else {
			spec = spec.nullValue("app_id");
		}
		if (entity.getServiceInstanceId() != null) {
			spec = spec.value("service_instance_id", entity.getServiceInstanceId());
		} else {
			spec = spec.nullValue("service_instance_id");
		}
		spec = spec.value("type", entity.getType());
		if (entity.getName() != null) {
			spec = spec.value("name", entity.getName());
		} else {
			spec = spec.nullValue("name");
		}
		return spec.fetch().rowsUpdated().then(Mono.just(entity));
	}

	public Flux<HistoricalRecord> findAll() {
		String select = "select pk, transaction_date_time, action_taken, organization, space, app_id, service_instance_id, type, name from historical_record order by transaction_datetime desc";
		return client.execute(select)
						.as(HistoricalRecord.class)
						.fetch()
						.all();
	}

}
