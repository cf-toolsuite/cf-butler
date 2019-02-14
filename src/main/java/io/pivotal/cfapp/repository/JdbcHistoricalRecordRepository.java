package io.pivotal.cfapp.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.HistoricalRecord;
import io.reactivex.Flowable;
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
		Flowable<Long> insert = database
									.update("insert into historical_record (transaction_datetime, action_taken, organization, space, app_id, service_id, type, name) values (?, ?, ?, ?, ?, ?, ?, ?)")
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
									.returnGeneratedKeys()
									.getAs(Long.class);
		return Mono.from(database
				.select("select id, transaction_datetime, action_taken, organization, space, app_id, service_id, type, name where id = ?")
				.parameterStream(insert)
				.get(rs -> fromResultSet(rs)));
	}

	public Flux<HistoricalRecord> findAll() {
		String selectAll = "select id, transaction_datetime, action_taken, organization, space, app_id, service_id, type, name from historical_record order by transaction_datetime desc";
		return Flux.from(database
							.select(selectAll)
							.get(rs -> fromResultSet(rs)));
	}

	private HistoricalRecord fromResultSet(ResultSet rs) throws SQLException {
		return HistoricalRecord
				.builder()
					.id(rs.getLong(1))
					.transactionDateTime(rs.getTimestamp(2) != null ? rs.getTimestamp(2).toLocalDateTime(): null)
					.actionTaken(rs.getString(3))
					.organization(rs.getString(4))
					.space(rs.getString(5))
					.appId(rs.getString(6))
					.serviceId(rs.getString(7))
					.type(rs.getString(8))
					.name(rs.getString(9))
					.build();
	}
}
