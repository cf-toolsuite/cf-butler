package io.pivotal.cfapp.config;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "cf.policies")
public class PoliciesSettings {

    private String provider = "dbms";
    private String uri;
    private String commit;
    private Set<String> filePaths;

    public boolean isVersionManaged() {
        return getProvider().equalsIgnoreCase("git");
    }
}