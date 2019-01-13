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
		String createOne = "insert into historical_record (datetime_removed, organization, space, id, type, name) values (?, ?, ?, ?, ?, ?)";
		return Flux.from(database
							.update(createOne)
							.parameters(
								entity.getDateTimeRemoved() != null ? Timestamp.valueOf(entity.getDateTimeRemoved()): null,
								entity.getOrganization(),
								entity.getSpace(),
								entity.getId(),
								entity.getType(),
								entity.getName()
							)
							.counts())
							.then(Mono.just(entity));
	}

	public Flux<HistoricalRecord> findAll() {
		String selectAll = "select datetime_removed, organization, space, id, type, name from historical_record order by datetime_removed desc";
		return Flux.from(database
							.select(selectAll)
							.get(rs -> fromResultSet(rs)));
	}

	private HistoricalRecord fromResultSet(ResultSet rs) throws SQLException {
		return HistoricalRecord
				.builder()
					.dateTimeRemoved(rs.getTimestamp(1) != null ? rs.getTimestamp(1).toLocalDateTime(): null)
					.organization(rs.getString(2))
					.space(rs.getString(3))
					.id(rs.getString(4))
					.type(rs.getString(5))
					.name(rs.getString(6))
					.build();
	}
}
