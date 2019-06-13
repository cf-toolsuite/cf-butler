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

	public static final String SYSTEM_ORG = "system";
	private static final Set<String> DEFAULT_BLACKLIST = Set.of(SYSTEM_ORG);
	private static final String[] KNOWN_BUILDPACKS = "apt,binary,clojure,dotnet,elixir,emberjs,erlang,go,haskell,hwc,java,jboss,jetty,liberty,markdown,mendix,meteor,nginx,nodejs,php,pyspark,python,r_buildpack,ruby,rust,staticfile,swift,tc,tomcat,tomee,virgo,weblogic".split(",");
	private static final Set<String> DEFAULT_BUILDPACKS = Set.of(KNOWN_BUILDPACKS);
	// user accounts are typically email addresses, so we'll define a regex to match on recognizable email pattern
	// @see https://howtodoinjava.com/regex/java-regex-validate-email-address/
	private static final String DEFAULT_ACCOUNT_REGEX = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";

	private String apiHost;
	private Set<String> buildpacks = DEFAULT_BUILDPACKS;
	private boolean sslValidationSkipped;
	private Integer connectionPoolSize;
    private String username;
    private String password;
    // this is the value of RefreshToken within ~/.cf/config.json after one authenticates w/ cf login -a {api_endpoint} -sso
	private String refreshToken;
	private String accountRegex;
	private Set<String> organizationBlackList = DEFAULT_BLACKLIST;

    public Set<String> getOrganizationBlackList() {
    	while (organizationBlackList.remove(""));
    	Set<String> nonEmptyBlacklist = CollectionUtils.isEmpty(organizationBlackList) ?
    			DEFAULT_BLACKLIST: organizationBlackList;
    	return merge(nonEmptyBlacklist, DEFAULT_BLACKLIST);
	}

	public String getAccountRegex() {
		return StringUtils.isNotBlank(accountRegex) ? accountRegex: DEFAULT_ACCOUNT_REGEX;
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

}
