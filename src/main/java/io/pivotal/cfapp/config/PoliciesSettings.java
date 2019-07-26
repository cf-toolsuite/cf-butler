package io.pivotal.cfapp.config;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@Getter
@ConfigurationProperties(prefix = "cf.policies")
public class PoliciesSettings {

    @Default
    private String provider = "dbms";
    private String uri;
    private String commit;
    private Set<String> filePaths;

    public boolean isVersionManaged() {
        return getProvider().equalsIgnoreCase("git");
    }


}