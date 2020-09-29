package io.pivotal.cfapp.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
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
