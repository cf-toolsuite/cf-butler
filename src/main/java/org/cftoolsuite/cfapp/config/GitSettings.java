package org.cftoolsuite.cfapp.config;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@Getter
@ConfigurationProperties(prefix = "cf.policies.git")
public class GitSettings {

    @Default
    private String uri = "";
    private String username;
    @Default
    private String password = "";
    private String commit;
    private Set<String> filePaths;

    public boolean isAuthenticated() {
        return StringUtils.isNotBlank(getUsername());
    }

    public boolean isPinnedCommit() {
        return StringUtils.isNotBlank(getCommit());
    }

    public boolean isVersionManaged() {
        return StringUtils.isNotBlank(uri);
    }

    @ConstructorBinding
    GitSettings(
        String uri,
        String username,
        String password,
        String commit,
        Set<String> filePaths) {
            this.uri = uri;
            this.username = username;
            this.password = password;
            this.commit = commit;
            this.filePaths = filePaths;
    }
}
