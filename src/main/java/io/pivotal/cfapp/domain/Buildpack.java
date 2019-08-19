package io.pivotal.cfapp.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Buildpack {

    private String id;
    private String name;
    private Integer position;
    private Boolean enabled;
    private Boolean locked;
    private String filename;
    private String stack;

    public String getVersion() {
        return filename.substring(filename.lastIndexOf('v')).replace(".zip", "");
    }
}