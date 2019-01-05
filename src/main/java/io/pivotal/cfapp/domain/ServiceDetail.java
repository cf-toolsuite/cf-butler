package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
@ToString
public class ServiceDetail {
    
    private String id;
    private String organization;
    private String space;
    private String name;
    private String service;
    private String description;
    private String plan;
    private String type;
    private String applications;
    private String lastOperation;
    private LocalDateTime lastUpdated;
    private String dashboardUrl;
    private String requestedState;
    
    public static String headers() {
        return String.join(",", "organization", "space", 
                "name", "service", "description", "plan", "type", "bound applications", "last operation", "last updated", "dashboard url", "requested state");
    }
    
    public static ServiceDetailBuilder from(ServiceDetail detail) {
        return ServiceDetail.builder()
                            .id(detail.getId())
                            .organization(detail.getOrganization())
                            .space(detail.getSpace())
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
    
    public String toCsv() {
        return String
                .join(",", wrap(getOrganization()), wrap(getSpace()), 
                        wrap(getName()), wrap(getService()),
                        wrap(getDescription()),
                        wrap(getPlan()), wrap(getType()), wrap(StringUtils.join(getApplications())),
                        wrap(getLastOperation()), 
                        wrap(getLastUpdated() != null ? getLastUpdated().toString(): ""),
                        wrap(getDashboardUrl()), wrap(getRequestedState()));
    }
    
    private String wrap(String value) {
        return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
    }
}
