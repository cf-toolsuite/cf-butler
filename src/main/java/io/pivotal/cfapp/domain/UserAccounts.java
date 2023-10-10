package io.pivotal.cfapp.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"organization", "space", "accounts"})
public class UserAccounts {

    public static String headers() {
        return String.join(",", "organization", "space", "user accounts");
    }

    private static String wrap(String value) {
        return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
    }

    @JsonProperty("organization")
    private String organization;

    @JsonProperty("space")
    private String space;

    @Default
    @JsonProperty("accounts")
    private Set<String> accounts = new HashSet<>();

    @JsonCreator
    public UserAccounts(
            @JsonProperty("organization") String organization,
            @JsonProperty("space") String space,
            @JsonProperty("accounts") Set<String> accounts
            ) {
        this.organization = organization;
        this.space = space;
        this.accounts = accounts;
    }

    public String toCsv() {
        return String.join(",", wrap(getOrganization()), wrap(getSpace()), wrap(String.join(",", getAccounts() != null ? getAccounts(): Collections.emptyList())));
    }
}
