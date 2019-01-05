package io.pivotal.cfapp.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
public class AppRequest {

	private String id;
    private String organization;
    private String space;
    private String appName;
    private String image;

    public static AppRequestBuilder from(AppRequest request) {
        return AppRequest
                .builder()
                	.id(request.getId())
                    .organization(request.getOrganization())
                    .space(request.getSpace())
                    .appName(request.getAppName())
                    .image(request.getImage());
    }
}
