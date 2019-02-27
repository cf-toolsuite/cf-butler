package io.pivotal.cfapp.repository;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.HistoricalRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcHistoricalRecordRepository {

	private DatabaseClient client;

	@Autowired
	public R2dbcHistoricalRecordRepository(DatabaseClient client) {
		this.client = client;
	}

	public Mono<HistoricalRecord> save(HistoricalRecord entity) {
		return client.insert().into("historical_record")
						.value("transaction_datetime", entity.getTransactionDateTime() != null ? Timestamp.valueOf(entity.getTransactionDateTime()): null)
						.value("action_taken", entity.getActionTaken())
						.value("organization", entity.getOrganization())
						.value("space", entity.getSpace())
						.value("app_id", entity.getAppId())
						.value("service_id", entity.getServiceId())
						.value("type", entity.getType())
						.value("name", entity.getName())
						.fetch()
						.rowsUpdated()
						.then(Mono.just(entity));
	}

	public Flux<HistoricalRecord> findAll() {
		return client.select().from("historical_record")
						.orderBy(Sort.by(Direction.DESC, "transaction_datetime"))
						.as(HistoricalRecord.class)
						.fetch()
						.all();
	}

}
