package io.pivotal.cfapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.HistoricalRecord;
import io.pivotal.cfapp.repository.JdbcHistoricalRecordRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Service
public class JdbcHistoricalRecordService implements HistoricalRecordService {

	private final JdbcHistoricalRecordRepository repo;
	
	@Autowired
	public JdbcHistoricalRecordService(JdbcHistoricalRecordRepository repo) {
		this.repo = repo;
	}
	
	@Override
	public Mono<HistoricalRecord> save(HistoricalRecord entity) {
		return repo.save(entity);
	}

	@Override
	public Flux<HistoricalRecord> findAll() {
		return repo.findAll();
	}

}
