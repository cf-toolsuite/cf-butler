package io.pivotal.cfapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "om")
public class OpsmanSettings {

    private String apiHost;
    private String username;
    private String password;
    private boolean enabled;

}