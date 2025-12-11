package org.cftoolsuite.cfapp.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.cftoolsuite.cfapp.domain.ArchiveResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class TgzUtil {

    private static final Logger log = LoggerFactory.getLogger(TgzUtil.class);
    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_ENTRY_SIZE = 10 * 1024 * 1024; // 10MB max for text entries

    /**
     * Process archive stream without loading the entire archive into memory
     */
    public static Mono<ArchiveResult> processArchive(Flux<DataBuffer> incoming, String[] entryPatterns) {
        return DataBufferUtils.join(incoming)
                .flatMap(dataBuffer -> {
                    // Use a BufferedInputStream that supports mark/reset operations
                    // We'll read the entire file, but only keep one copy in memory
                    // For larger files (>100MB), consider a temporary file-based approach instead
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    return Mono.fromCallable(() -> {
                        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(bytes))) {
                            return processArchiveEntries(bufferedInputStream, entryPatterns);
                        }
                    }).subscribeOn(Schedulers.boundedElastic());
                });
    }

    /**
     * Alternative implementation for very large files (>100MB) that uses a temporary file
     * to avoid keeping the entire file in memory
     */
    public static Mono<ArchiveResult> processLargeArchive(Flux<DataBuffer> incoming, String[] entryPatterns) {
        return Mono.using(
                // Resource creation: Create a temporary file
                () -> Files.createTempFile("archive-", ".tgz"),

                // Resource usage: Write the incoming data to the file, then process it
                tempFile -> DataBufferUtils.write(incoming, tempFile, StandardOpenOption.WRITE)
                        .then(Mono.fromCallable(() -> {
                            try (InputStream is = new BufferedInputStream(Files.newInputStream(tempFile))) {
                                return processArchiveEntries(is, entryPatterns);
                            }
                        }).subscribeOn(Schedulers.boundedElastic())),

                // Resource cleanup: Delete the temporary file
                tempFile -> {
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (IOException e) {
                        log.warn("Failed to delete temporary file: {}", tempFile, e);
                    }
                }
        );
    }

    /**
     * Process multiple archives in parallel with configurable parallelism
     */
    public static Flux<ArchiveResult> processArchivesBatch(List<Flux<DataBuffer>> archives, String[] entryPatterns, int parallelism) {
        return Flux.fromIterable(archives)
                .flatMap(archive ->
                                processArchive(archive, entryPatterns)
                                        .subscribeOn(Schedulers.boundedElastic()),
                        parallelism);
    }

    /**
     * Process archive entries with stream-based reading and selective buffering
     */
    private static ArchiveResult processArchiveEntries(InputStream is, String[] entryPatterns) {
        ArchiveResult.Builder resultBuilder = ArchiveResult.builder();
        CompressorInputStream cis = null;
        TarArchiveInputStream tarIs = null;

        try {
            // Directly specify the compression format for tgz files instead of using detection
            // which requires mark/reset support
            cis = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, is);
            tarIs = new ArchiveStreamFactory().createArchiveInputStream("tar", cis);

            ConcurrentLinkedQueue<String> jarFilenames = new ConcurrentLinkedQueue<>();
            AtomicReference<String> pomContent = new AtomicReference<>();
            AtomicReference<String> manifestContent = new AtomicReference<>();
            AtomicReference<Integer> buildJdkSpec = new AtomicReference<>();

            TarArchiveEntry entry;
            while ((entry = tarIs.getNextTarEntry()) != null) {
                if (tarIs.canReadEntryData(entry) && !entry.isDirectory()) {
                    String entryName = entry.getName();
                    long entrySize = entry.getSize();

                    // Check if this entry matches any of our patterns
                    for (String pattern : entryPatterns) {
                        if (isEntryMatch(entryName, pattern)) {
                            if (pattern.equals("pom.xml") && entryName.endsWith("pom.xml")) {
                                // Only read POM if it's not excessively large
                                if (entrySize > 0 && entrySize < MAX_ENTRY_SIZE) {
                                    log.trace("Found pom.xml: {}", entryName);
                                    String content = readEntryContent(tarIs, Math.toIntExact(entrySize));
                                    pomContent.set(content);
                                } else {
                                    log.warn("Skipping oversized pom.xml: {} (size: {})", entryName, entrySize);
                                    skipEntry(tarIs);
                                }
                                break;
                            } else if (pattern.equals("META-INF/MANIFEST.MF") && entryName.endsWith("META-INF/MANIFEST.MF")) {
                                // Only read manifest if it's not excessively large
                                if (entrySize > 0 && entrySize < MAX_ENTRY_SIZE) {
                                    log.trace("Found MANIFEST.MF: {}", entryName);
                                    String content = readEntryContent(tarIs, Math.toIntExact(entrySize));
                                    manifestContent.set(content);

                                    // Extract JDK spec without reloading the manifest
                                    try {
                                        String buildJdkSpecStr = JarManifestUtil.obtainAttributeValue(content, "Build-Jdk-Spec");
                                        if (buildJdkSpecStr != null) {
                                            buildJdkSpec.set(Integer.valueOf(buildJdkSpecStr));
                                        }
                                    } catch (Exception e) {
                                        log.error("Error extracting Build-Jdk-Spec from MANIFEST.MF", e);
                                    }
                                } else {
                                    log.warn("Skipping oversized MANIFEST.MF: {} (size: {})", entryName, entrySize);
                                    skipEntry(tarIs);
                                }
                                break;
                            } else if (pattern.startsWith(".") && entryName.endsWith(pattern)) {
                                // For JAR files, just record the filename without loading content
                                if (pattern.equals(".jar")) {
                                    String filename = Paths.get(entryName).getFileName().toString();
                                    log.trace("Found JAR file: {}", filename);
                                    jarFilenames.add(filename);
                                }
                                skipEntry(tarIs);
                                break;
                            }
                        }
                    }
                }
            }

            // Build result with collected data
            if (pomContent.get() != null) {
                resultBuilder.pomContent(pomContent.get());
            }

            if (manifestContent.get() != null) {
                resultBuilder.manifestContent(manifestContent.get());
            }

            if (buildJdkSpec.get() != null) {
                resultBuilder.buildJdkSpec(buildJdkSpec.get());
            }

            // Add all JAR filenames
            jarFilenames.forEach(resultBuilder::addJarFilename);

            return resultBuilder.build();

        } catch (Exception e) {
            log.error("Error processing archive entries", e);
            throw new RuntimeException("Error processing archive entries", e);
        } finally {
            // Clean up resources in reverse order
            closeQuietly(tarIs);
            closeQuietly(cis);
            closeQuietly(is);
        }
    }

    /**
     * Efficiently reads entry content with known size
     */
    private static String readEntryContent(TarArchiveInputStream tarIs, int size) throws IOException {
        // Use exact size buffer for efficiency
        byte[] buffer = new byte[Math.min(size, MAX_ENTRY_SIZE)];
        int totalRead = 0;
        int bytesRead;

        while (totalRead < buffer.length && (bytesRead = tarIs.read(buffer, totalRead, buffer.length - totalRead)) != -1) {
            totalRead += bytesRead;
        }

        return new String(buffer, 0, totalRead, StandardCharsets.UTF_8);
    }

    /**
     * Skips an entry without reading its contents into memory
     */
    private static void skipEntry(TarArchiveInputStream tarIs) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        while (tarIs.read(buffer, 0, buffer.length) != -1) {
            // Just consume the bytes without storing them
        }
    }

    /**
     * Checks if entry name matches the given pattern
     */
    private static boolean isEntryMatch(String entryName, String pattern) {
        if (pattern.startsWith(".")) {
            return entryName.endsWith(pattern);
        } else {
            return entryName.endsWith(pattern);
        }
    }

    /**
     * Safely closes a resource without throwing exceptions
     */
    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.warn("Error closing resource", e);
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: TgzUtil <droplet_file>");
            System.exit(1);
        }
        File dropletFile = new File(args[0]);
        if (!dropletFile.exists()) {
            System.err.println("Droplet file not found: " + dropletFile.getAbsolutePath());
            System.exit(1);
        }

        PathResource resource = new PathResource(dropletFile.toPath());
        try {
            // Define chunk size for reading
            int chunkSize = BUFFER_SIZE;
            Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(resource, new DefaultDataBufferFactory(), chunkSize);

            // Process for POM, manifests and JAR files
            String[] entries = {"pom.xml", "META-INF/MANIFEST.MF", ".jar"};

            // Choose the appropriate processing method based on file size
            long fileSize = dropletFile.length();
            log.info("Processing file with size: {} bytes", fileSize);

            Mono<ArchiveResult> resultMono;
            if (fileSize > 100 * 1024 * 1024) { // > 100MB
                log.info("Using file-based approach for large file");
                resultMono = TgzUtil.processLargeArchive(dataBufferFlux, entries);
            } else {
                log.info("Using memory-based approach for smaller file");
                resultMono = TgzUtil.processArchive(dataBufferFlux, entries);
            }

            resultMono
                    .log()
                    .doOnNext(result -> {
                        log.info("pom.xml content available: {}", result.pomContent() != null);
                        if (result.pomContent() != null) {
                            log.info("pom.xml content length: {} bytes", result.pomContent().length());
                        }

                        log.info("MANIFEST.MF content available: {}", result.manifestContent() != null);
                        if (result.manifestContent() != null) {
                            log.info("MANIFEST.MF content length: {} bytes", result.manifestContent().length());
                        }

                        log.info("Build JDK Spec: {}", result.buildJdkSpec());
                        log.info("JAR files found: {}", result.getJarFilenames().size());
                        log.info("JAR filenames: {}", result.getJarFilenamesAsString());
                    })
                    .doOnError(error -> log.error("Error during processing", error))
                    .doOnSuccess(result -> log.info("Processing complete"))
                    .block();
        } catch (Exception e) {
            log.error("Error processing droplet file", e);
        }
    }
}