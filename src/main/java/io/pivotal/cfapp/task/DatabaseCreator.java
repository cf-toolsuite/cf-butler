package io.pivotal.cfapp.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.DbmsSettings;
import io.pivotal.cfapp.task.DatabaseCreatedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DatabaseCreator implements ApplicationRunner {

	private final DatabaseClient client;
	private final ResourceLoader resourceLoader;
	private final DbmsSettings settings;
	private final ApplicationEventPublisher publisher;

	@Autowired
	public DatabaseCreator(
		DatabaseClient client,
		ResourceLoader resourceLoader,
		DbmsSettings settings,
		ApplicationEventPublisher publisher) {
		this.client = client;
		this.resourceLoader = resourceLoader;
		this.settings = settings;
		this.publisher = publisher;
	}

	@Override
	public void run(ApplicationArguments args) {
		String line; String provider = ""; String ddl = ""; String location = "";
		try {
			provider = settings.getProvider().toLowerCase().replaceAll("\\s","");
			location = String.join("/", "classpath:db", provider, "schema.ddl");
			Resource schema = resourceLoader.getResource(location);
			InputStream is = schema.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				if (!line.isBlank()) {
					ddl = line.strip().replace(";","");
					client.execute(ddl)
						.then()
						.doOnError(e -> {
							log.error(e.getMessage());
							System.exit(1);
						}).subscribe();
				}
			}
			br.close();
			publisher.publishEvent(new DatabaseCreatedEvent(this));
		} catch (IOException ioe) {
			log.error(String.format("Failed trying to read %s\n", location), ioe);
			System.exit(1);
		}
	}

}
