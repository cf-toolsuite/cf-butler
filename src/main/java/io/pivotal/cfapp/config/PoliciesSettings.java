package io.pivotal.cfapp.config;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import lombok.Getter;


@Getter
@ConstructorBinding
@ConfigurationProperties(prefix = "cf.policies")
public class PoliciesSettings {

    private final String provider;
    private final String uri;
    private final String commit;
    private final Set<String> filePaths;


    public PoliciesSettings(
        @DefaultValue(value="dbms") String provider,
        String uri,
        String commit,
        Set<String> filePaths
    ) {
        this.provider = provider;
        this.uri = uri;
        this.commit = commit;
        this.filePaths = filePaths;
    }

    public boolean isVersionManaged() {
        return getProvider().equalsIgnoreCase("git");
    }

}