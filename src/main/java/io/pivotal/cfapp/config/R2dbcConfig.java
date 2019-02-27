package io.pivotal.cfapp.config;

import java.net.URI;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.r2dbc.dialect.Dialect;
import org.springframework.data.r2dbc.dialect.H2Dialect;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.function.DefaultReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;

import io.jsonwebtoken.lang.Assert;
import io.pivotal.cfapp.config.ButlerSettings.DbmsSettings;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;

@Configuration
public class R2dbcConfig {

    @Bean
    public ConnectionFactory connectionFactory(
        DataSourceProperties properties, DbmsSettings settings) {
        String cleanURI = properties.getUrl().substring(5);
        URI uri = URI.create(cleanURI);
        ConnectionFactory factory = null;
        switch (settings.getProvider()) {
            case "h2":
                factory = new H2ConnectionFactory(
                    H2ConnectionConfiguration
                        .builder()
                            .url(properties.getUrl())
                            .username(properties.getUsername())
                            .password(properties.getPassword())
                            .build());
                break;
            case "postgres":
                factory = new PostgresqlConnectionFactory(
                    PostgresqlConnectionConfiguration
                        .builder()
                            .applicationName("cf-butler")
                            .host(uri.getHost() == null ? "localhost": uri.getHost())
                            .username(properties.getUsername())
                            .password(properties.getPassword())
                            .build());
                break;
        }
        Assert.notNull(factory, "DbmsSettings::provider must be one of ['h2', 'postgres'] in order to properly initialize ConnectionFactory!");
        return factory;
    }

    @Bean
    public DatabaseClient databaseClient(
        DataSourceProperties properties, DbmsSettings settings) {
        return DatabaseClient
                .builder()
                    .connectionFactory(connectionFactory(properties, settings))
                    .build();
    }

    @Bean
    public MappingContext mappingContext() {
        final RelationalMappingContext relationalMappingContext = new RelationalMappingContext();
        relationalMappingContext.afterPropertiesSet();
        return relationalMappingContext;
    }

    @Bean
    public Dialect dialect(DbmsSettings settings) {
        Dialect dialect = null;
        switch (settings.getProvider()) {
            case "h2":
                dialect = H2Dialect.INSTANCE;
                break;
            case "postgres":
                dialect = PostgresDialect.INSTANCE;
                break;
        }
        Assert.notNull(dialect, "DbmsSettings::provider must be one of ['h2', 'postgres'] in order to properly initialize Dialect!");
        return dialect;
    }

    @Bean
    public R2dbcRepositoryFactory repositoryFactory(
        DataSourceProperties properties, DbmsSettings settings) {
        return new R2dbcRepositoryFactory(
            databaseClient(properties, settings),
            mappingContext(),
            new DefaultReactiveDataAccessStrategy(dialect(settings)));
    }
}