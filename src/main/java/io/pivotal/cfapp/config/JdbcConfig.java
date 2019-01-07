package io.pivotal.cfapp.config;

import javax.sql.DataSource;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("jdbc")
@Configuration
public class JdbcConfig {

	@Bean
	Database database(DataSource dataSource) {
		return Database
					.nonBlocking()
					.connectionProvider(dataSource)
					.maxPoolSize(Runtime.getRuntime().availableProcessors() * 5)
					.build();
	}
}
