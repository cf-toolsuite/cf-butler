package io.pivotal.cfapp.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JarSetFilterReader implements JavaArtifactReader {

    private Map<String, String> filters = new HashMap<>();

    public JarSetFilterReader(Map<String, String> filters) {
        this.filters = filters;
    }

    public Set<String> read(String jars) {
        if (jars != null && !jars.isEmpty()) {
            return Arrays.stream(jars.split("\n"))
                         .filter(jar -> filters.keySet().stream().anyMatch(key -> jar.startsWith(key)))
                         .map(this::formatJarName)
                         .collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }

    private String formatJarName(String jarName) {
        String group = findGroupForJar(jarName);
        if (!group.isEmpty()) {
            int dashIndex = jarName.lastIndexOf('-');
            int dotIndex = jarName.lastIndexOf('.');
            if (dashIndex != -1 && dotIndex != -1) {
                //String key = jarName.substring(0, dashIndex);
                String version = jarName.substring(dashIndex + 1, dotIndex);
                //return String.format("%s:%s:%s", group, key, version);
                return String.format("%s:%s", group, version);
            }
        }
        return "";
    }

    private String findGroupForJar(String jarName) {
        for (String key : filters.keySet()) {
            if (jarName.startsWith(key)) {
                return filters.get(key);
            }
        }
        return "";
    }

    public String type() {
        return "jar";
    }
}