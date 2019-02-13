package io.pivotal.cfapp.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

import org.davidmoten.rx.jdbc.Database;
import org.davidmoten.rx.jdbc.exceptions.SQLRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Profile("jdbc")
@Component
public class DatabaseCreator implements ApplicationRunner {

	private final Database database;
	private final ResourceLoader resourceLoader;

	@Autowired
	public DatabaseCreator(Database database, ResourceLoader resourceLoader) {
		this.database = database;
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		try (Connection c = database.connection().blockingGet()) {
			c.setAutoCommit(true);
			Resource schema = resourceLoader.getResource("classpath:db/hsql/schema.sql");
			InputStream is = schema.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line; String ddl;
			while ((line = br.readLine()) != null) {
				if (!line.isBlank()) {
					ddl = line.strip().replace(";","");
					c.prepareStatement(ddl).execute();
				}
			}
			br.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);

		} catch (SQLException sqle) {
			throw new SQLRuntimeException(sqle);
		}
	}

}
