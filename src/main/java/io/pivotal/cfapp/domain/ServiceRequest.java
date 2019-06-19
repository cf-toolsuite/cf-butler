package io.pivotal.cfapp.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Builder.Default;

@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
@Getter
@ToString
public class ServiceRequest {

	private String id;
    private String organization;
    private String space;
    private String serviceName;

    @Default
    private List<String> applicationIds = new ArrayList<>();

    @Default
    private List<String> applicationNames = new ArrayList<>();

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
