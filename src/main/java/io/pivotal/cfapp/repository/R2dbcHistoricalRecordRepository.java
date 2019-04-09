package io.pivotal.cfapp.repository;

import java.sql.Timestamp;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.function.DatabaseClient.GenericInsertSpec;
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
				.value("transaction_datetime", Timestamp.valueOf(entity.getTransactionDateTime()));
		spec = spec.value("action_taken", entity.getActionTaken());
		spec = spec.value("organization", entity.getOrganization());
		spec = spec.value("space", entity.getSpace());
		if (entity.getAppId() != null) {
			spec = spec.value("app_id", entity.getAppId());
		} else {
			spec = spec.nullValue("app_id", String.class);
		}
		if (entity.getServiceInstanceId() != null) {
			spec = spec.value("service_instance_id", entity.getServiceInstanceId());
		} else {
			spec = spec.nullValue("service_instance_id", String.class);
		}
		spec = spec.value("type", entity.getType());
		if (entity.getName() != null) {
			spec = spec.value("name", entity.getName());
		} else {
			spec = spec.nullValue("name", String.class);
		}
		return spec.fetch().rowsUpdated().then(Mono.just(entity));
	}

	public Flux<HistoricalRecord> findAll() {
		return client.select().from("historical_record")
						.orderBy(Sort.by(Direction.DESC, "transaction_datetime"))
						.as(HistoricalRecord.class)
						.fetch()
						.all();
	}

}
