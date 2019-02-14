package io.pivotal.cfapp.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.ButlerSettings.DbmsSettings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("jdbc")
@Component
public class DatabaseCreator implements ApplicationRunner {

	private final Database database;
	private final ResourceLoader resourceLoader;
	private final DbmsSettings settings;

	@Autowired
	public DatabaseCreator(Database database, ResourceLoader resourceLoader,
			DbmsSettings settings) {
		this.database = database;
		this.resourceLoader = resourceLoader;
		this.settings = settings;
	}

	@Override
	public void run(ApplicationArguments args) {
		String line; String ddl = ""; String location = "";
		try (Connection c = database.connection().blockingGet()) {
			c.setAutoCommit(true);
			location = String.join("/", "classpath:db", settings.getProvider(), "schema.ddl");
			Resource schema = resourceLoader.getResource(location);
			InputStream is = schema.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				if (!line.isBlank()) {
					ddl = line.strip().replace(";","");
					c.prepareStatement(ddl).execute();
				}
			}
			br.close();
		} catch (IOException ioe) {
			log.error(String.format("Failed trying to read %s\n", location), ioe);
			System.exit(1);

		} catch (SQLException sqle) {
			log.error(String.format("Failed trying to execute '%s'\n", ddl), sqle);
			System.exit(1);
		}
	}

}
