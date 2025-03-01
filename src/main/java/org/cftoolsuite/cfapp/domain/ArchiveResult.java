package org.cftoolsuite.cfapp.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * ArchiveResult captures the extracted data from archive processing
 * Uses immutable pattern for thread safety
 */
public class ArchiveResult {
    private final String pomContent;
    private final String manifestContent;
    private final Integer buildJdkSpec;
    private final List<String> jarFilenames;

    private ArchiveResult(Builder builder) {
        this.pomContent = builder.pomContent;
        this.manifestContent = builder.manifestContent;
        this.buildJdkSpec = builder.buildJdkSpec;
        this.jarFilenames = builder.jarFilenames != null ?
                Collections.unmodifiableList(new ArrayList<>(builder.jarFilenames)) :
                Collections.emptyList();
    }

    public String pomContent() {
        return pomContent;
    }

    public String manifestContent() {
        return manifestContent;
    }

    public Integer buildJdkSpec() {
        return buildJdkSpec;
    }

    public List<String> getJarFilenames() {
        return jarFilenames;
    }

    /**
     * Backward compatibility for tests
     */
    public List<String> jarFilenames() {
        return jarFilenames;
    }

    public String getJarFilenamesAsString() {
        return jarFilenames.stream()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Creates a merged ArchiveResult containing data from both this result and another
     */
    public ArchiveResult merge(ArchiveResult other) {
        if (other == null) {
            return this;
        }

        Builder builder = new Builder();

        // Use this object's values as defaults, override with other if present
        builder.pomContent(this.pomContent);
        if (other.pomContent() != null) {
            builder.pomContent(other.pomContent());
        }

        builder.manifestContent(this.manifestContent);
        if (other.manifestContent() != null) {
            builder.manifestContent(other.manifestContent());
        }

        builder.buildJdkSpec(this.buildJdkSpec);
        if (other.buildJdkSpec() != null) {
            builder.buildJdkSpec(other.buildJdkSpec());
        }

        // Combine jar filenames from both results
        for (String filename : this.jarFilenames) {
            builder.addJarFilename(filename);
        }

        for (String filename : other.jarFilenames) {
            builder.addJarFilename(filename);
        }

        return builder.build();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ArchiveResult.class.getSimpleName() + "[", "]")
                .add("pomContent=" + (pomContent != null ? "present" : "absent"))
                .add("manifestContent=" + (manifestContent != null ? "present" : "absent"))
                .add("buildJdkSpec=" + buildJdkSpec)
                .add("jarFilenamesCount=" + jarFilenames.size())
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ArchiveResult
     */
    public static class Builder {
        private String pomContent;
        private String manifestContent;
        private Integer buildJdkSpec;
        private List<String> jarFilenames;

        public Builder pomContent(String pomContent) {
            this.pomContent = pomContent;
            return this;
        }

        public Builder manifestContent(String manifestContent) {
            this.manifestContent = manifestContent;
            return this;
        }

        public Builder buildJdkSpec(Integer buildJdkSpec) {
            this.buildJdkSpec = buildJdkSpec;
            return this;
        }

        public Builder addJarFilename(String jarFilename) {
            if (this.jarFilenames == null) {
                this.jarFilenames = new ArrayList<>();
            }
            this.jarFilenames.add(jarFilename);
            return this;
        }

        public ArchiveResult build() {
            return new ArchiveResult(this);
        }
    }
}