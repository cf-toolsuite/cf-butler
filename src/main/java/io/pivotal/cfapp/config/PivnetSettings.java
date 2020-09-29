package io.pivotal.cfapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "pivnet")
public class PivnetSettings {

    private String apiToken;
    private boolean enabled;
}
