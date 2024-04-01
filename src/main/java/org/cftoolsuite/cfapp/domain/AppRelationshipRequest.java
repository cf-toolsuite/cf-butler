package org.cftoolsuite.cfapp.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AppRelationshipRequest {

    public static List<AppRelationshipRequest> listOf(ServiceInstanceDetail detail) {
        List<AppRelationshipRequest> result = new ArrayList<>();
        detail.getApplications().forEach(a -> {
            result.add(AppRelationshipRequest
                    .builder()
                    .organization(detail.getOrganization())
                    .space(detail.getSpace())
                    .applicationName(a)
                    .serviceInstanceId(detail.getServiceInstanceId())
                    .serviceName(detail.getName())
                    .serviceOffering(detail.getService())
                    .serviceType(detail.getType())
                    .servicePlan(detail.getPlan())
                    .build());
        });
        return result;
    }
    private String organization;
    private String space;
    private String serviceInstanceId;
    private String serviceName;
    private String serviceOffering;
    private String applicationId;
    private String applicationName;
    private String serviceType;

    private String servicePlan;
}
