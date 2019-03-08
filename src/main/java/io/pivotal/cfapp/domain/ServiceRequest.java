package io.pivotal.cfapp.domain;

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
public class ServiceRequest {

	private String id;
    private String organization;
    private String space;
    private String serviceName;
    private List<String> applicationIds;
    private List<String> applicationNames;

    public static ServiceRequestBuilder from(ServiceRequest request) {
        return ServiceRequest
                .builder()
                	.id(request.getId())
                    .organization(request.getOrganization())
                    .space(request.getSpace())
                    .serviceName(request.getServiceName())
                    .applicationIds(request.getApplicationIds())
                    .applicationNames(request.getApplicationNames());
    }
}
