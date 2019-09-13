package io.pivotal.cfapp.domain;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.springframework.util.CollectionUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Builder
@Getter
@JsonPropertyOrder({ "labels", "annotations" })
// @see https://docs.pivotal.io/pivotalcf/2-5/adminguide/metadata.html
public class EmbeddedMetadata {

    private static final String ALPHANUMERIC_REGEX = "^[a-zA-Z0-9.-]*$";
    private static final String ALPHANUMERIC_REGEX_AND_UNDERSCORE = "^[a-zA-Z0-9._-]*$";

    @Singular
    @JsonProperty("labels")
    private Map<String, String> labels;

    @Singular
    @JsonProperty("annotations")
    private Map<String, String> annotations;

    @JsonCreator
    public EmbeddedMetadata(
        @JsonProperty("labels") Map<String, String> labels,
        @JsonProperty("annotations") Map<String, String> annotations
    ) {
        this.labels = labels;
        this.annotations = annotations;
    }

    @JsonIgnore
    public boolean isValid() {
        Set<String> filteredLabelKeys = CollectionUtils.isEmpty(getLabels()) ? Collections.emptySet(): getLabels().keySet().stream().filter(k -> isValidKey(k)).collect(Collectors.toSet());
        Set<String> filteredAnnotationKeys = CollectionUtils.isEmpty(getAnnotations()) ? Collections.emptySet(): getAnnotations().keySet().stream().filter(k -> isValidKey(k)).collect(Collectors.toSet());
        Set<String> filteredLabelValues = CollectionUtils.isEmpty(getLabels()) ? Collections.emptySet(): getLabels().values().stream().filter(v -> isValidLabelValue(v)).collect(Collectors.toSet());
        Set<String> filteredAnnotationValues = CollectionUtils.isEmpty(getAnnotations()) ? Collections.emptySet(): getAnnotations().values().stream().filter(v -> isValidAnnotationValue(v)).collect(Collectors.toSet());
        boolean labelKeysAreValid = false;
        boolean annotationKeysAreValid = false;
        boolean labelValuesAreValid = false;
        boolean annotationValuesAreValid = false;
        if (getLabels() == null && getAnnotations() == null) {
            return true;
        }
        if (getLabels() != null) {
            labelKeysAreValid = filteredLabelKeys.size() == (getLabels().keySet() == null ? 0: getLabels().keySet().size());
            labelValuesAreValid = filteredLabelValues.size() == (getLabels().values() == null ? 0: getLabels().values().size());
        }
        if (getAnnotations() != null) {
            annotationKeysAreValid = filteredAnnotationKeys.size() == (getAnnotations().keySet() == null ? 0: getAnnotations().keySet().size());
            annotationValuesAreValid = filteredAnnotationValues.size() == (getAnnotations().values() == null ? 0: getAnnotations().values().size());
        }
        return labelKeysAreValid && annotationKeysAreValid && labelValuesAreValid && annotationValuesAreValid;
    }

    private static boolean isValidKey(String key) {
        if (key == null) {
            return false;
        }
        if (key.contains("/")) {
            String[] keyParts = key.split("/");
            return
                keyParts[0].length() >= 0 && keyParts[0].length() <= 253
                    && patternAdherent(keyParts[0], ALPHANUMERIC_REGEX)
                    && keyParts[1].length() >= 1 && keyParts[1].length() <= 63
                    && patternAdherent(keyParts[1], ALPHANUMERIC_REGEX_AND_UNDERSCORE);
        } else {
            return
                key.length() >= 1 && key.length() <= 63
                    && patternAdherent(key, ALPHANUMERIC_REGEX_AND_UNDERSCORE);
        }
    }

    private static boolean isValidLabelValue(String value) {
        if (value == null) {
            return false;
        }
        return
            value.length() >= 0 && value.length() <= 63
                && patternAdherent(value, ALPHANUMERIC_REGEX_AND_UNDERSCORE);
    }

    private static boolean isValidAnnotationValue(String value) {
        if (value == null) {
            return false;
        }
        return value.length() >= 0 && value.length() <= 5000;
    }

    private static boolean patternAdherent(String value, String pattern) {
        if (value == null) {
            return false;
        }
        return value.matches(pattern);
    }

}