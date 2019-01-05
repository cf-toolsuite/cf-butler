package io.pivotal.cfapp;

import javax.annotation.PostConstruct;

import org.hsqldb.util.DatabaseManagerSwing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

@Profile("jdbc")
@Component
public class HSqlConsole {

	private final DataSourceProperties dataSourceProperties;
	private final ConfigurableEnvironment environment;

	@Autowired
	public HSqlConsole(
			DataSourceProperties dataSourceProperties,
			ConfigurableEnvironment environment) {
		this.dataSourceProperties = dataSourceProperties;
		this.environment = environment;
	}
	
	@PostConstruct
	public void launchDatabaseManager() throws Exception {
		if (isSet(environment, "java.awt.headless", "false")) {
			DatabaseManagerSwing.main(
					new String[] { 
							"--url", dataSourceProperties.getUrl(), 
							"--user", dataSourceProperties.getUsername(), 
							"--password", dataSourceProperties.getPassword()
					});
		}
	}
	
	private boolean isSet(ConfigurableEnvironment environment, String property, String expectedPropertyValue) {
	    String value = environment.getProperty(property);
	    return (value != null && value.equalsIgnoreCase(expectedPropertyValue));
	}

}
