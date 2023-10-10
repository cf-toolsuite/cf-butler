package io.pivotal.cfapp.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Buildpack {

    private String id;
    private String name;
    private Integer position;
    private Boolean enabled;
    private Boolean locked;
    private String filename;
    private String stack;

    public String getVersion() {
        String version = null;
        int versionPosition = filename.lastIndexOf('v');
        if (versionPosition >= 0) {
            version = filename.substring(versionPosition).replace(".zip", "");
        }
        return version;
    }
}
