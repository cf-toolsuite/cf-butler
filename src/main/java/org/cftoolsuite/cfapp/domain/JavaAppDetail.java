package org.cftoolsuite.cfapp.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@EqualsAndHashCode
@ToString
@Table("java_application_detail")
public class JavaAppDetail {

    @Id
    @JsonIgnore
    private Long pk;
    private String organization;
    private String space;
    private String appId;
    private String appName;
    private String dropletId;
    private String pomContents;
    private String jars;
    private String springDependencies;

    public static JavaAppDetailBuilder from(JavaAppDetail detail) {
        return JavaAppDetail
                .builder()
                .pk(detail.getPk())
                .organization(detail.getOrganization())
                .space(detail.getSpace())
                .appId(detail.getAppId())
                .appName(detail.getAppName())
                .dropletId(detail.getDropletId())
                .pomContents(detail.getPomContents())
                .jars(detail.getJars())
                .springDependencies(detail.getSpringDependencies());
    }

    public static JavaAppDetailBuilder from(AppDetail detail) {
        return JavaAppDetail
                .builder()
                .pk(detail.getPk())
                .organization(detail.getOrganization())
                .space(detail.getSpace())
                .appId(detail.getAppId())
                .appName(detail.getAppName());
    }

    public static String headers() {
        return String.join(",", "organization", "space", "application id", "application name", "droplet id", "pom contents", "jars", "spring dependencies" );
    }

    private static String wrap(String value) {
        return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
    }

    public String toCsv() {
        return String.join(",", wrap(getOrganization()), wrap(getSpace()), wrap(getAppId()), wrap(getAppName()),
                wrap(getDropletId()), wrap(getPomContents()), wrap(getJars()), wrap(getSpringDependencies()));
    }

}
