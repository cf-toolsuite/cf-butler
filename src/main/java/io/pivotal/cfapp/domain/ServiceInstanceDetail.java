package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@EqualsAndHashCode
@ToString
@Table("service_instance_detail")
public class ServiceInstanceDetail {

    @Id
    @JsonIgnore
    private Long pk;
    private String organization;
    private String space;
    private String serviceInstanceId;
    @Column("service_name")
    private String name;
    private String service;

    private String description;

    private String plan;
    private String type;
    @Default
    @Column("bound_applications")
    private List<String> applications = new ArrayList<>();
    private String lastOperation;

    private LocalDateTime lastUpdated;

    private String dashboardUrl;

    private String requestedState;

    public static ServiceInstanceDetailBuilder from(ServiceInstanceDetail detail) {
        return ServiceInstanceDetail
                .builder()
                .pk(detail.getPk())
                .organization(detail.getOrganization())
                .space(detail.getSpace())
                .serviceInstanceId(detail.getServiceInstanceId())
                .name(detail.getName())
                .service(detail.getService())
                .description(detail.getDescription())
                .plan(detail.getPlan())
                .type(detail.getType())
                .applications(detail.getApplications())
                .lastOperation(detail.getLastOperation())
                .lastUpdated(detail.getLastUpdated())
                .dashboardUrl(detail.getDashboardUrl())
                .requestedState(detail.getRequestedState());
    }

    public static String headers() {
        return String.join(",", "organization", "space", "service instance id",
                "name", "service", "description", "plan", "type", "bound applications", "last operation", "last updated", "dashboard url", "requested state");
    }

    private static String wrap(String value) {
        return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
    }

    public String toCsv() {
        return String.join(",", wrap(getOrganization()), wrap(getSpace()), wrap(getServiceInstanceId()), wrap(getName()),
                wrap(getService()), wrap(getDescription()), wrap(getPlan()), wrap(getType()),
                wrap(String.join(",", getApplications() != null ? getApplications(): Collections.emptyList())), wrap(getLastOperation()),
                wrap(getLastUpdated() != null ? getLastUpdated().toString() : ""), wrap(getDashboardUrl()),
                wrap(getRequestedState()));
    }

}
