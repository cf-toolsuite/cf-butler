package org.cftoolsuite.cfapp.service;

import org.cftoolsuite.cfapp.domain.Space;
import org.cftoolsuite.cfapp.domain.UserSpaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class UserSpacesService {

    private static Space buildSpace(String organization, String space) {
        return Space
                .builder()
                .organizationName(organization)
                .spaceName(space)
                .build();
    }

    private final SpaceUsersService service;

    @Autowired
    public UserSpacesService(SpaceUsersService service) {
        this.service = service;
    }

    public Mono<UserSpaces> getUserSpaces(String name) {
        return service
                .findByAccountName(name)
                .map(su -> buildSpace(su.getOrganization(), su.getSpace()))
                .collectList()
                .map(spaces -> UserSpaces.builder().accountName(name).spaces(spaces).build());
    }
}
