package org.cftoolsuite.cfapp.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.cftoolsuite.cfapp.domain.ArchiveResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class TgzUtilTest {

    private File tempFile;
    private Path tempDirPath;
    private static final int TEST_FILE_SIZE_THRESHOLD = 5 * 1024 * 1024; // 5MB for testing

    @BeforeEach
    void setUp() throws IOException {
        tempDirPath = Files.createTempDirectory("tgzutil-test");
        tempFile = File.createTempFile("test-droplet", ".tgz", tempDirPath.toFile());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempFile != null && tempFile.exists()) {
            Files.delete(tempFile.toPath());
        }
        if (tempDirPath != null && tempDirPath.toFile().exists()) {
            recursiveDelete(tempDirPath);
        }
    }

    private void recursiveDelete(Path path) throws IOException {
        Files.walk(path)
                .sorted((p1, p2) -> -p1.compareTo(p2))
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        throw new RuntimeException("Error deleting path: " + p, e);
                    }
                });
    }

    @Test
    void testExtractFileContent() throws IOException {
        String filename = "test.txt";
        String content = "This is a test file.";
        createTestTgzFile(List.of(new TestFile(filename, content)));

        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(tempFile.toPath(), factory, 1024);

        // Use the appropriate method based on file size
        Flux<ArchiveResult> resultFlux = getProcessingFlux(dataBufferFlux, new String[]{filename});

        // No POM content expected, but checking for completeness
        StepVerifier.create(resultFlux)
                .expectNextMatches(Objects::nonNull)
                .expectComplete()
                .verify();
    }

    @Test
    void testExtractPomContent() throws IOException {
        String filename = "pom.xml";
        String content = "<project>...</project>";
        createTestTgzFile(List.of(new TestFile(filename, content)));

        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(tempFile.toPath(), factory, 1024);

        // Use the appropriate method based on file size
        Flux<ArchiveResult> resultFlux = getProcessingFlux(dataBufferFlux, new String[]{filename});

        StepVerifier.create(resultFlux)
                .expectNextMatches(result -> result.pomContent() != null && result.pomContent().equals(content))
                .expectComplete()
                .verify();
    }

    @Test
    void testExtractManifestContent() throws IOException {
        String filename = "META-INF/MANIFEST.MF";
        String content = "Manifest-Version: 1.0\nBuild-Jdk-Spec: 11\n";
        createTestTgzFile(List.of(new TestFile(filename, content)));

        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(tempFile.toPath(), factory, 1024);

        // Use the appropriate method based on file size
        Flux<ArchiveResult> resultFlux = getProcessingFlux(dataBufferFlux, new String[]{filename});

        StepVerifier.create(resultFlux)
                .expectNextMatches(result ->
                        result.manifestContent() != null &&
                                result.manifestContent().equals(content) &&
                                result.buildJdkSpec() != null &&
                                result.buildJdkSpec() == 11)
                .expectComplete()
                .verify();
    }

    @Test
    void testFindMatchingFiles() throws IOException {
        List<TestFile> files = Arrays.asList(
                new TestFile("test.txt", "Test Text")
        );
        createTestTgzFileWithJar(files);
        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(tempFile.toPath(), factory, 1024);

        // The issue is here - we need to test for .jar files, not .txt files
        // since only .jar files are added to jarFilenames in ArchiveResult
        Flux<ArchiveResult> resultFlux = getProcessingFlux(dataBufferFlux, new String[]{".jar"});

        StepVerifier.create(resultFlux)
                .expectNextMatches(result -> {
                    // We expect an empty result since there are no .jar files
                    return result.getJarFilenames() != null &&
                            result.getJarFilenames().isEmpty();
                })
                .expectComplete()
                .verify();
    }

    @Test
    void testFindJarFiles() throws IOException {
        List<TestFile> files = Arrays.asList(
                new TestFile("lib/spring-core.jar", "JAR Content"),
                new TestFile("lib/spring-context.jar", "JAR Content")
        );
        createTestTgzFile(files);
        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(tempFile.toPath(), factory, 1024);

        // Use the appropriate method based on file size
        Flux<ArchiveResult> resultFlux = getProcessingFlux(dataBufferFlux, new String[]{".jar"});

        StepVerifier.create(resultFlux)
                .expectNextMatches(result -> {
                    return result.getJarFilenames() != null &&
                            result.getJarFilenames().size() == 2 &&
                            result.getJarFilenames().contains("spring-core.jar") &&
                            result.getJarFilenames().contains("spring-context.jar");
                })
                .expectComplete()
                .verify();
    }

    @Test
    void testFindMatchingFilesNoneFound() throws IOException {
        List<TestFile> files = Arrays.asList(
                new TestFile("test1.txt", "Test TXT 1"),
                new TestFile("test2.txt", "Test TXT 2")
        );
        createTestTgzFile(files);
        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(tempFile.toPath(), factory, 1024);

        // Use the appropriate method based on file size
        Flux<ArchiveResult> resultFlux = getProcessingFlux(dataBufferFlux, new String[]{".jar"});

        StepVerifier.create(resultFlux)
                .expectNextMatches(result -> result.getJarFilenames().isEmpty())
                .expectComplete()
                .verify();
    }

    @Test
    void testMultipleFileTypes() throws IOException {
        List<TestFile> files = Arrays.asList(
                new TestFile("pom.xml", "<project>...</project>"),
                new TestFile("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\nBuild-Jdk-Spec: 17\n"),
                new TestFile("lib/spring-core.jar", "JAR Content"),
                new TestFile("lib/spring-context.jar", "JAR Content")
        );
        createTestTgzFile(files);

        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(tempFile.toPath(), factory, 1024);

        // Use the appropriate method based on file size
        Flux<ArchiveResult> resultFlux = getProcessingFlux(dataBufferFlux,
                new String[]{"pom.xml", "META-INF/MANIFEST.MF", ".jar"});

        StepVerifier.create(resultFlux)
                .expectNextMatches(result -> {
                    return result.pomContent() != null &&
                            result.pomContent().equals("<project>...</project>") &&
                            result.manifestContent() != null &&
                            result.manifestContent().contains("Build-Jdk-Spec: 17") &&
                            result.buildJdkSpec() == 17 &&
                            result.getJarFilenames().size() == 2 &&
                            result.getJarFilenames().contains("spring-core.jar") &&
                            result.getJarFilenames().contains("spring-context.jar");
                })
                .expectComplete()
                .verify();
    }

    @Test
    void testLargeFileProcessing() throws IOException {
        // This test demonstrates using the file-based approach for larger files
        List<TestFile> files = Arrays.asList(
                new TestFile("pom.xml", "<project>large-file-test</project>"),
                new TestFile("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\nBuild-Jdk-Spec: 17\n"),
                new TestFile("lib/test.jar", "JAR Content")
        );

        // Create a temp file that's small, but we'll force the large file processing path
        createTestTgzFile(files);

        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(tempFile.toPath(), factory, 1024);

        // Directly test the large file processing path
        StepVerifier.create(TgzUtil.processLargeArchive(dataBufferFlux,
                        new String[]{"pom.xml", "META-INF/MANIFEST.MF", ".jar"}))
                .expectNextMatches(result ->
                        result.pomContent() != null &&
                                result.pomContent().equals("<project>large-file-test</project>") &&
                                result.getJarFilenames().size() == 1)
                .expectComplete()
                .verify();
    }

    @Test
    void testArchiveResultBuilder() {
        // Test the builder pattern directly
        ArchiveResult result = ArchiveResult.builder()
                .pomContent("<project>test</project>")
                .addJarFilename("test1.jar")
                .addJarFilename("test2.jar")
                .manifestContent("Manifest-Version: 1.0\nBuild-Jdk-Spec: 11\n")
                .buildJdkSpec(11)
                .build();

        // Verify the builder creates correct objects
        assert result.pomContent().equals("<project>test</project>");
        assert result.getJarFilenames().size() == 2;
        assert result.getJarFilenames().contains("test1.jar");
        assert result.getJarFilenames().contains("test2.jar");
        assert result.manifestContent().equals("Manifest-Version: 1.0\nBuild-Jdk-Spec: 11\n");
        assert result.buildJdkSpec() == 11;

        // Test toString method for coverage
        String toString = result.toString();
        assert toString.contains("ArchiveResult");
        assert toString.contains("present"); // For content fields
    }

    /**
     * Helper method to select the appropriate processing method based on file size
     */
    private Flux<ArchiveResult> getProcessingFlux(Flux<DataBuffer> dataBufferFlux, String[] entryPatterns) {
        if (tempFile.length() > TEST_FILE_SIZE_THRESHOLD) {
            return TgzUtil.processLargeArchive(dataBufferFlux, entryPatterns).flux();
        } else {
            return TgzUtil.processArchive(dataBufferFlux, entryPatterns).flux();
        }
    }

    private void createTestTgzFileWithJar(List<TestFile> files) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
             GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(fileOutputStream);
             TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(gzipOutputStream)) {
            tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            for (TestFile file : files) {
                if (file.getFilename().endsWith(".jar")) {
                    byte[] jarContent = createTestJarFile(file.getContent());
                    createTarEntry(file.getFilename(), jarContent, tarArchiveOutputStream);
                } else {
                    createTarEntry(file.getFilename(), file.getContent().getBytes(StandardCharsets.UTF_8), tarArchiveOutputStream);
                }
            }
            tarArchiveOutputStream.finish();
        }
    }

    private void createTarEntry(String name, byte[] content, TarArchiveOutputStream tarArchiveOutputStream) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(content.length);
        tarArchiveOutputStream.putArchiveEntry(entry);
        tarArchiveOutputStream.write(content);
        tarArchiveOutputStream.closeArchiveEntry();
    }

    private void createTestTgzFile(List<TestFile> files) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
             GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(fileOutputStream);
             TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(gzipOutputStream)) {
            tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            for (TestFile file : files) {
                createTarEntry(file.getFilename(), file.getContent().getBytes(StandardCharsets.UTF_8), tarArchiveOutputStream);
            }
            tarArchiveOutputStream.finish();
        }
    }

    private byte[] createTestJarFile(String content) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             JarOutputStream jos = new JarOutputStream(baos, createManifest())) {

            JarEntry entry = new JarEntry("test.txt");
            jos.putNextEntry(entry);
            jos.write(content.getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();

            return baos.toByteArray();
        }
    }

    private Manifest createManifest() {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        return manifest;
    }

    private static class TestFile {
        private final String filename;
        private final String content;

        public TestFile(String filename, String content) {
            this.filename = filename;
            this.content = content;
        }

        public String getFilename() {
            return filename;
        }

        public String getContent() {
            return content;
        }
    }
}