package io.pivotal.cfapp.config;

import org.cloudfoundry.uaa.tokens.GrantType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "om")
public class OpsmanSettings {

    private String apiHost;
    private String clientId = "opsman";
    private String clientSecret = "";
    private String username;
    private String password;
    private GrantType grantType = GrantType.PASSWORD;
    private boolean enabled;

}
