package io.pivotal.cfapp.config;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.Builder;
import lombok.Getter;
import lombok.Builder.Default;

@Builder
@Getter
@ConstructorBinding
@ConfigurationProperties(prefix = "cf.policies.git")
public class GitSettings {

    @Default
    private final String uri = "";
    private final String username;
    @Default
    private final String password = "";
    private final String commit;
    private final Set<String> filePaths;

    public boolean isAuthenticated() {
        return StringUtils.isNotBlank(getUsername());
    }

    public boolean isVersionManaged() {
        return StringUtils.isNotBlank(uri);
    }

    public boolean isPinnedCommit() {
        return StringUtils.isNotBlank(getCommit());
    }

}