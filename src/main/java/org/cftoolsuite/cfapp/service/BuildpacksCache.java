package org.cftoolsuite.cfapp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.cfapp.domain.Buildpack;
import org.springframework.stereotype.Component;

@Component
public class BuildpacksCache {

    private final Map<String, Buildpack> buildpacksById = new HashMap<>();

    public Map<String, Buildpack> from(List<org.cloudfoundry.operations.buildpacks.Buildpack> input) {
        buildpacksById.clear();
        input.forEach(
                b -> {
                    Buildpack buildpack =
                            Buildpack
                            .builder()
                            .id(b.getId())
                            .name(b.getName())
                            .position(b.getPosition())
                            .enabled(b.getEnabled())
                            .locked(b.getLocked())
                            .filename(b.getFilename())
                            .build();
                    buildpacksById.put(b.getId(), buildpack);
                });
        return buildpacksById;
    }

    public Buildpack getBuildpackById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return buildpacksById.get(id);
    }
}
