package org.cftoolsuite.cfapp.domain;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmbeddedMetadataTest {

    @Test
    public void assertThatEmbeddedMetadataOnlyWithAnnotationIsValid() {
        EmbeddedMetadata m =
                EmbeddedMetadata
                .builder()
                .annotation("annotation-name", "annotation-value")
                .build();
        Assertions.assertTrue(m.isValid());
    }

    @Test
    public void assertThatEmbeddedMetadataOnlyWithLabelIsValid() {
        EmbeddedMetadata m =
                EmbeddedMetadata
                .builder()
                .label("label-name", "label-value")
                .build();
        Assertions.assertTrue(m.isValid());
    }

    @Test
    public void assertThatEmbeddedMetadataWithExceptionalAnnotationIsInvalid() {
        EmbeddedMetadata m =
                EmbeddedMetadata
                .builder()
                .annotation("annotation&name", "annotation-value")
                .build();
        Assertions.assertFalse(m.isValid());
    }

    @Test
    public void assertThatEmbeddedMetadataWithExceptionalLabelIsInvalid() {
        EmbeddedMetadata m =
                EmbeddedMetadata
                .builder()
                .label("label&name", "label-value")
                .build();
        Assertions.assertFalse(m.isValid());
    }

    @Test
    public void assertThatEmbeddedMetadataWithLabelAndAnnotationIsValid() {
        EmbeddedMetadata m =
                EmbeddedMetadata
                .builder()
                .label("label-name", "label-value")
                .annotation("annotation-name", "annotation-value")
                .build();
        Assertions.assertTrue(m.isValid());
    }

    @Test
    public void assertThatEmbeddedMetadataWithLongAnnotationKeyIsInvalid() {
        EmbeddedMetadata m =
                EmbeddedMetadata
                .builder()
                .annotation(RandomStringUtils.randomAlphanumeric(100), "annotation-value")
                .build();
        Assertions.assertFalse(m.isValid());
    }

    @Test
    public void assertThatEmbeddedMetadataWithLongAnnotationValueIsInvalid() {
        EmbeddedMetadata m =
                EmbeddedMetadata
                .builder()
                .annotation(RandomStringUtils.randomAlphanumeric(15), RandomStringUtils.randomAlphanumeric(7500))
                .build();
        Assertions.assertFalse(m.isValid());
    }

    @Test
    public void assertThatEmbeddedMetadataWithLongLabelKeyIsInvalid() {
        EmbeddedMetadata m =
                EmbeddedMetadata
                .builder()
                .label(RandomStringUtils.randomAlphanumeric(100), "label-value")
                .build();
        Assertions.assertFalse(m.isValid());
    }

    @Test
    public void assertThatEmbeddedMetadataWithLongLabelValueIsInvalid() {
        EmbeddedMetadata m =
                EmbeddedMetadata
                .builder()
                .label(RandomStringUtils.randomAlphanumeric(15), RandomStringUtils.randomAlphanumeric(100))
                .build();
        Assertions.assertFalse(m.isValid());
    }

    @Test
    public void assertThatEmbeddedMetadataWithNamespacedKeyForAnnotationIsValid() {
        EmbeddedMetadata m =
                EmbeddedMetadata
                .builder()
                .annotation("annotation.prefix/annotation-name", "annotation-value")
                .build();
        Assertions.assertTrue(m.isValid());
    }

    @Test
    public void assertThatEmbeddedMetadataWithNamespacedKeyForLabelIsValid() {
        EmbeddedMetadata m =
                EmbeddedMetadata
                .builder()
                .label("label.prefix/label-name", "label-value")
                .build();
        Assertions.assertTrue(m.isValid());
    }

    @Test
    public void assertThatEmbeddedMetadataWithoutLabelsAndAnnotationsIsValid() {
        EmbeddedMetadata m =
                EmbeddedMetadata
                .builder()
                .build();
        Assertions.assertTrue(m.isValid());
    }
}
