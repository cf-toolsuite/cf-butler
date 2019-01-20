package io.pivotal.cfapp.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.HistoricalRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Repository
public class JdbcHistoricalRecordRepository {

	private Database database;

	@Autowired
	public JdbcHistoricalRecordRepository(Database database) {
		this.database = database;
	}

	public Mono<HistoricalRecord> save(HistoricalRecord entity) {
		String createOne = "insert into historical_record (transaction_datetime, action_taken, organization, space, app_id, service_id, type, name) values (?, ?, ?, ?, ?, ?, ?, ?)";
		return Mono.from(database
							.update(createOne)
							.parameters(
								entity.getTransactionDateTime() != null ? Timestamp.valueOf(entity.getTransactionDateTime()): null,
								entity.getActionTaken(),
								entity.getOrganization(),
								entity.getSpace(),
								entity.getAppId(),
								entity.getServiceId(),
								entity.getType(),
								entity.getName()
							)
							.counts()
							.map(r -> entity));
	}

	public Flux<HistoricalRecord> findAll() {
		String selectAll = "select transaction_datetime, action_taken, organization, space, app_id, service_id, type, name from historical_record order by transaction_datetime desc";
		return Flux.from(database
							.select(selectAll)
							.get(rs -> fromResultSet(rs)));
	}

	private HistoricalRecord fromResultSet(ResultSet rs) throws SQLException {
		return HistoricalRecord
				.builder()
					.transactionDateTime(rs.getTimestamp(1) != null ? rs.getTimestamp(1).toLocalDateTime(): null)
					.actionTaken(rs.getString(2))
					.organization(rs.getString(3))
					.space(rs.getString(4))
					.appId(rs.getString(5))
					.serviceId(rs.getString(6))
					.type(rs.getString(7))
					.name(rs.getString(8))
					.build();
	}
}
