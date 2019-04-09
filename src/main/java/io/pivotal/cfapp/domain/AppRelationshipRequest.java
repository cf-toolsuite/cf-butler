package io.pivotal.cfapp.domain;

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

    private String organization;
    private String space;
    private String serviceInstanceId;
    private String serviceName;
    private String applicationId;
    private String applicationName;

    public static AppRelationshipRequestBuilder from(AppRelationshipRequest request) {
        return AppRelationshipRequest
                .builder()
                    .organization(request.getOrganization())
                    .space(request.getSpace())
                    .serviceInstanceId(request.getServiceInstanceId())
                    .serviceName(request.getServiceName())
                    .applicationId(request.getApplicationId())
                    .applicationName(request.getApplicationName());
    }
}
