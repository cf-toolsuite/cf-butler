package io.pivotal.cfapp.config;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
	private static final String[] KNOWN_BUILDPACKS = "apt,binary,clojure,dotnet,elixir,emberjs,erlang,go,haskell,hwc,java,jboss,jetty,liberty,meteor,nginx,nodejs,php,pyspark,python,ruby,rust,staticfile,swift,tc,tomcat,tomee,weblogic".split(",");
	private static final Set<String> DEFAULT_BUILDPACKS = Set.of(KNOWN_BUILDPACKS);

	private String apiHost;
	private Set<String> buildpacks = DEFAULT_BUILDPACKS;
	private boolean sslValidationSkipped;
	private Integer connectionPoolSize;
    private String username;
    private String password;
    // this is the value of RefreshToken within ~/.cf/config.json after one authenticates w/ cf login -a {api_endpoint} -sso
    private String refreshToken;
	private Set<String> organizationBlackList = DEFAULT_BLACKLIST;

    public Set<String> getOrganizationBlackList() {
    	while (organizationBlackList.remove(""));
    	Set<String> nonEmptyBlacklist = CollectionUtils.isEmpty(organizationBlackList) ?
    			DEFAULT_BLACKLIST: organizationBlackList;
    	return merge(nonEmptyBlacklist, DEFAULT_BLACKLIST);
	}

	public String getUsageDomain() {
		return "https://" + getApiHost().replace("api.", "app-usage.");
	}

	public Set<String> getBuildpacks() {
		while (buildpacks.remove(""));
		Set<String> nonEmptyBuildpacks = CollectionUtils.isEmpty(buildpacks) ?
    			DEFAULT_BUILDPACKS: buildpacks;
    	return merge(nonEmptyBuildpacks, DEFAULT_BUILDPACKS);
	}

	public String getBuildpack(String input, String image) {
        if (!StringUtils.isBlank(image)) {
            return null;
        } else if (StringUtils.isBlank(input)) {
            return "anomalous";
        } else {
			Optional<String> buildpack = 
				getBuildpacks()
					.stream()
					.filter(b -> input.contains(b))
					.collect(Collectors.reducing((a, b) -> null));
            return buildpack.orElse("unknown");
        }
    }

    private Set<String> merge(Set<String> source, Set<String> defaultList) {
    	Set<String> result = new HashSet<>();
    	result.addAll(source);
    	result.addAll(defaultList);
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
