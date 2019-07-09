package io.pivotal.cfapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryAutoConfiguration;
import org.springframework.stereotype.Component;

import io.r2dbc.spi.ConnectionFactory;

@Component
@AutoConfigureAfter(ConnectionFactoryAutoConfiguration.class)
public class DbmsSettings {

    private final ConnectionFactory factory;

    @Autowired
    public DbmsSettings(ConnectionFactory factory) {
        this.factory = factory;
    }

    public String getProvider() {
        return factory.getMetadata().getName();
    }
}
