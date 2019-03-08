package io.pivotal.cfapp.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.r2dbc.spi.ConnectionFactory;

@Component
public class DbmsSettings {

    private final ConnectionFactory factory;

    @Autowired
    public DbmsSettings(ConnectionFactory factory) {
        this.factory = factory;
    }

    public String getProvider() {
        return factory.getMetadata().getName();
    }

    public String getBindPrefix() {
        return IndexPrefix.from(getProvider()).getSymbol();
    }

    enum IndexPrefix {
        // provider for  must sync with ConnectionFactoryMetadata::getName
        H2("H2","$"),
        POSTGRESQL("PostgreSQL","$"),
        MSSQL("Microsoft SQL Server","@");

        private final String provider;
        private final String symbol;

        IndexPrefix(String provider, String symbol){
            this.provider = provider;
            this.symbol = symbol;
        }

        String getProvider() {
            return provider;
        }
        String getSymbol() {
            return symbol;
        }

        public static IndexPrefix from(String provider) {
            return Arrays.asList(IndexPrefix.values())
                    .stream()
                        .filter(f -> f.getProvider().equalsIgnoreCase(provider))
                        .collect(Collectors.toSet())
                            .iterator()
                            .next();
        }
    }
}
