package io.pivotal.cfapp.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JarSetFilterReader implements JavaArtifactReader {

    private Map<String, String> filters;

    public JarSetFilterReader(Map<String, String> filters) {
        this.filters = filters;
    }

    public Set<String> read(String jars) {
        if (jars != null && !jars.isEmpty()) {
            Map<String, String> latestVersions = new HashMap<>();
            Arrays.stream(jars.split("\n"))
                    .filter(jar -> filters.keySet().stream().anyMatch(jar::startsWith))
                    .forEach(jar -> {
                        String group = findGroupForJar(jar);
                        if (!group.isEmpty()) {
                            int dashIndex = jar.lastIndexOf('-');
                            int dotIndex = jar.lastIndexOf('.');
                            if (dashIndex != -1 && dotIndex != -1) {
                                String version = jar.substring(dashIndex + 1, dotIndex);
                                String currentLatestVersion = latestVersions.getOrDefault(group, "");
                                if (currentLatestVersion.isEmpty() || isNewerVersion(version, currentLatestVersion)) {
                                    latestVersions.put(group, version);
                                }
                            }
                        }
                    });
            return latestVersions.entrySet().stream()
                    .map(entry -> String.format("%s:%s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }

    private boolean isNewerVersion(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");
        int minLength = Math.min(parts1.length, parts2.length);
        for (int i = 0; i < minLength; i++) {
            int v1 = Integer.parseInt(parts1[i]);
            int v2 = Integer.parseInt(parts2[i]);
            if (v1 != v2) {
                return v1 > v2;
            }
        }
        return parts1.length > parts2.length;
    }

    private String findGroupForJar(String jarName) {
        for (String key : filters.keySet()) {
            if (jarName.startsWith(key)) {
                return filters.get(key);
            }
        }
        return "";
    }

    public String mode() {
        return "list-jars";
    }
}