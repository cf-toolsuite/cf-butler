package io.pivotal.cfapp.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "cf")
public class ButlerSettings {

    private String apiHost;
    private String username;
    private String password;
    private String passcode;
    private List<String> organizationBlackList;

}
