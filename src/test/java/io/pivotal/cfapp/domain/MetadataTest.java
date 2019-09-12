package io.pivotal.cfapp.domain;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetadataTest {

    @Test
    public void assertThatMetadataWithLabelAndAnnotationIsValid() {
        Metadata m =
            Metadata
                .builder()
                    .label("label-name", "label-value")
                    .annotation("annotation-name", "annotation-value")
                    .build();
        Assertions.assertTrue(m.isValid());
    }

    @Test
    public void assertThatMetadataWithoutLabelsAndAnnotationsIsValid() {
        Metadata m =
            Metadata
                .builder()
                    .build();
        Assertions.assertTrue(m.isValid());
    }

    @Test
    public void assertThatMetadataOnlyWithLabelIsValid() {
        Metadata m =
            Metadata
                .builder()
                    .label("label-name", "label-value")
                    .build();
        Assertions.assertTrue(m.isValid());
    }

    @Test
    public void assertThatMetadataOnlyWithAnnotationIsValid() {
        Metadata m =
            Metadata
                .builder()
                    .annotation("annotation-name", "annotation-value")
                    .build();
        Assertions.assertTrue(m.isValid());
    }

    @Test
    public void assertThatMetadataWithExceptionalLabelIsInvalid() {
        Metadata m =
            Metadata
                .builder()
                    .label("label&name", "label-value")
                    .build();
        Assertions.assertFalse(m.isValid());
    }

    @Test
    public void assertThatMetadataWithExceptionalAnnotationIsInvalid() {
        Metadata m =
            Metadata
                .builder()
                    .annotation("annotation&name", "annotation-value")
                    .build();
        Assertions.assertFalse(m.isValid());
    }

    @Test
    public void assertThatMetadataWithNamespacedKeyForLabelIsValid() {
        Metadata m =
            Metadata
                .builder()
                    .label("label.prefix/label-name", "label-value")
                    .build();
        Assertions.assertTrue(m.isValid());
    }

    @Test
    public void assertThatMetadataWithNamespacedKeyForAnnotationIsValid() {
        Metadata m =
            Metadata
                .builder()
                    .annotation("annotation.prefix/annotation-name", "annotation-value")
                    .build();
        Assertions.assertTrue(m.isValid());
    }

    @Test
    public void assertThatMetadataWithLongLabelKeyIsInvalid() {
        Metadata m =
            Metadata
                .builder()
                    .label(RandomStringUtils.randomAlphanumeric(100), "label-value")
                    .build();
        Assertions.assertFalse(m.isValid());
    }

    @Test
    public void assertThatMetadataWithLongAnnotationKeyIsInvalid() {
        Metadata m =
            Metadata
                .builder()
                    .annotation(RandomStringUtils.randomAlphanumeric(100), "annotation-value")
                    .build();
        Assertions.assertFalse(m.isValid());
    }

    @Test
    public void assertThatMetadataWithLongLabelValueIsInvalid() {
        Metadata m =
            Metadata
                .builder()
                    .label(RandomStringUtils.randomAlphanumeric(15), RandomStringUtils.randomAlphanumeric(100))
                    .build();
        Assertions.assertFalse(m.isValid());
    }

    @Test
    public void assertThatMetadataWithLongAnnotationValueIsInvalid() {
        Metadata m =
            Metadata
                .builder()
                    .annotation(RandomStringUtils.randomAlphanumeric(15), RandomStringUtils.randomAlphanumeric(7500))
                    .build();
        Assertions.assertFalse(m.isValid());
    }
}