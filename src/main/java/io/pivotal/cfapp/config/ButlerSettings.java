package io.pivotal.cfapp.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "cf")
public class ButlerSettings {

	private static final String SYSTEM_ORG = "system";
	private static final Set<String> DEFAULT_BLACKLIST = Set.of(SYSTEM_ORG);

    private String apiHost;
    private boolean sslValidationSkipped;
    private String username;
    private String password;
    // this is the value of RefreshToken within ~/.cf/config.json after one authenticates w/ cf login -a {api_endpoint} -sso
    private String refreshToken;
    private Set<String> organizationBlackList;

    public Set<String> getOrganizationBlackList() {
    	while (organizationBlackList.remove(""));
    	Set<String> nonEmptyBlacklist = CollectionUtils.isEmpty(organizationBlackList) ?
    			DEFAULT_BLACKLIST: organizationBlackList;
    	return merge(nonEmptyBlacklist);
    }

    private Set<String> merge(Set<String> orgBlackList) {
    	Set<String> result = new HashSet<>();
    	result.addAll(orgBlackList);
    	result.addAll(DEFAULT_BLACKLIST);
    	return result;
    }

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

}
