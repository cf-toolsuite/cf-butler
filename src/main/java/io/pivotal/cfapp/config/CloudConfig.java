package io.pivotal.cfapp.config;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("cloud")
@Configuration
class CloudConfig {

    private static final List<String> SUPPORTED_SCHEMES = Arrays.asList(new String[] { "mysql", "postgresql"});
    private static final String VCAP_SERVICE_VARIABLE = "vcap.services.cf-butler-backend.credentials.uri";

    @Autowired
    private Environment env;

    @Bean
    @ConditionalOnProperty(VCAP_SERVICE_VARIABLE)
    ConnectionFactory connectionFactory() {
        R2dbcProperties properties = r2dbcProperties();
        ConnectionFactoryOptions.Builder builder = ConnectionFactoryOptions
                .parse(properties.determineUrl()).mutate();
        String username = properties.determineUsername();
        if (StringUtils.hasText(username)) {
            builder.option(ConnectionFactoryOptions.USER, username);
        }
        String password = properties.determinePassword();
        if (StringUtils.hasText(password)) {
            builder.option(ConnectionFactoryOptions.PASSWORD, password);
        }
        String databaseName = properties.determineDatabaseName();
        if (StringUtils.hasText(databaseName)) {
            builder.option(ConnectionFactoryOptions.DATABASE, databaseName);
        }
        if (properties.getProperties() != null) {
            properties.getProperties()
                    .forEach((key, value) -> builder
                            .option(Option.valueOf(key), value));
        }
        return ConnectionFactories.get(builder.build());
    }

    // support for external R2DBC source is limited to providers that support URI scheme
    private R2dbcProperties r2dbcProperties() {
        URI uri = env.getRequiredProperty(VCAP_SERVICE_VARIABLE, URI.class);
        String scheme = uri.getScheme();
        log.info("Attempting to connnect to a {} database instance.", scheme);
        if (scheme.startsWith("postgres")) {
            scheme = "postgresql";
        }
        if (SUPPORTED_SCHEMES.contains(scheme)) {
            R2dbcProperties properties = new R2dbcProperties();
            properties.setName(uri.getPath().replaceAll("/",""));
            String[] userInfoParts = uri.getUserInfo().split(":");
            String username = userInfoParts[0];
            String password = userInfoParts[1];
            properties.setUsername(username);
            properties.setPassword(password);
            StringBuilder builder = new StringBuilder();
            builder.append(String.format("r2dbc:pool:%s://", scheme));
            builder.append(uri.getHost());
            if (uri.getPort() != -1) {
                builder.append(":" + uri.getPort());
            }
            builder.append(uri.getPath());
            properties.setUrl(builder.toString());
            return properties;
        } else {
            throw new IllegalStateException(String.format("Could not initialize R2DBC properties from bound %s service instance.", uri.getScheme()));
        }
    }
}