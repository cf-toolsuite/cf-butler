package io.pivotal.cfapp.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@Getter
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
public class UserRequest {

    private String organization;
    private String spaceName;

    public static UserRequestBuilder from(UserRequest request) {
        return UserRequest
                .builder()
                    .organization(request.getOrganization())
                    .spaceName(request.getSpaceName());
    }
}
