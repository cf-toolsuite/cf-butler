package org.cftoolsuite.cfapp.util;

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
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class TgzUtil {

    private static final Logger log = LoggerFactory.getLogger(TgzUtil.class);
    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_ENTRY_SIZE = 10 * 1024 * 1024; // 10MB max for text entries

    /**
     * Process archive stream with minimal blocking operations
     */
    public static Mono<ArchiveResult> processArchive(Flux<DataBuffer> incoming, String[] entryPatterns) {
        return DataBufferUtils.join(incoming)
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    return processArchiveEntriesNonBlocking(bytes, entryPatterns);
                });
    }

    /**
     * Process large archives using completely non-blocking operations where possible
     * This implementation focuses on minimizing blocking calls to avoid thread starvation
     */
    public static Mono<ArchiveResult> processLargeArchive(Flux<DataBuffer> incoming, String[] entryPatterns) {
        // Create temp file path asynchronously
        return createTempFileAsync("archive-", ".tgz")
                .flatMap(tempFile -> {
                    // Write incoming data to temp file non-blockingly
                    return DataBufferUtils.write(incoming, tempFile, StandardOpenOption.WRITE)
                            // Read file content non-blockingly
                            .then(readFileContentAsync(tempFile))
                            // Process archive content
                            .flatMap(content -> processArchiveEntriesNonBlocking(content, entryPatterns))
                            // Clean up temp file when done
                            .doFinally(signal -> deleteFileAsync(tempFile)
                            .subscribe());
                });
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
     * Process archive data using non-blocking NIO operations
     * @param data Byte array containing the archive data
     * @param entryPatterns Patterns to match for archive entries
     * @return Archive result containing extracted data
     */
    private static Mono<ArchiveResult> processArchiveEntriesNonBlocking(byte[] data, String[] entryPatterns) {
        return Mono.fromCallable(() -> {
                    // Unfortunately, Commons Compress doesn't offer a non-blocking API
                    // We need to use its InputStream-based API even with NIO
                    try (InputStream is = new ByteArrayInputStream(data);
                         CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, is);
                         TarArchiveInputStream tarIs = new ArchiveStreamFactory().createArchiveInputStream("tar", cis)) {

                        ArchiveResult.Builder resultBuilder = ArchiveResult.builder();
                        TarArchiveEntry entry;

                        while ((entry = tarIs.getNextEntry()) != null) {
                            if (tarIs.canReadEntryData(entry) && !entry.isDirectory()) {
                                processEntry(entry, tarIs, resultBuilder, entryPatterns);
                            }
                        }

                        return resultBuilder.build();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Process a single tar entry
     */
    private static void processEntry(TarArchiveEntry entry, TarArchiveInputStream tarIs,
                                     ArchiveResult.Builder resultBuilder, String[] entryPatterns) throws IOException {
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
                        resultBuilder.pomContent(content);
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
                        resultBuilder.manifestContent(content);

                        // Extract JDK spec without reloading the manifest
                        try {
                            String buildJdkSpecStr = JarManifestUtil.obtainAttributeValue(content, "Build-Jdk-Spec");
                            if (buildJdkSpecStr != null) {
                                resultBuilder.buildJdkSpec(Integer.valueOf(buildJdkSpecStr));
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
                        resultBuilder.addJarFilename(filename);
                    }
                    skipEntry(tarIs);
                    break;
                }
            }
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
     * Read file content asynchronously with minimal blocking
     */
    private static Mono<byte[]> readFileContentAsync(Path filePath) {
        return Mono.using(
                // Open file channel asynchronously
                () -> AsynchronousFileChannel.open(filePath, StandardOpenOption.READ),

                // Use the channel
                channel -> Mono.create(sink -> {
                    try {
                        long fileSize = channel.size();
                        if (fileSize > Integer.MAX_VALUE) {
                            sink.error(new IllegalArgumentException("File too large to process in memory: " + fileSize + " bytes"));
                            return;
                        }

                        ByteBuffer buffer = ByteBuffer.allocate((int)fileSize);
                        readChannelAsync(channel, buffer, 0, sink);
                    } catch (IOException e) {
                        sink.error(e);
                    }
                }),

                // Close channel
                channel -> {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        log.warn("Error closing channel", e);
                    }
                }
        );
    }

    /**
     * Read from channel asynchronously at position into buffer
     */
    private static void readChannelAsync(AsynchronousFileChannel channel, ByteBuffer buffer,
                                         long position, MonoSink<byte[]> sink) {
        channel.read(buffer, position, null, new CompletionHandler<Integer, Void>() {
            private long totalBytesRead = 0;

            @Override
            public void completed(Integer bytesRead, Void attachment) {
                if (bytesRead == -1) {
                    // End of file reached
                    buffer.flip();
                    byte[] result = new byte[buffer.remaining()];
                    buffer.get(result);
                    sink.success(result);
                    return;
                }

                totalBytesRead += bytesRead;

                if (buffer.hasRemaining()) {
                    // Continue reading if buffer has space
                    channel.read(buffer, position + totalBytesRead, null, this);
                } else {
                    // Buffer full, return result
                    buffer.flip();
                    byte[] result = new byte[buffer.remaining()];
                    buffer.get(result);
                    sink.success(result);
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                sink.error(exc);
            }
        });
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
        return entryName.endsWith(pattern);
    }

    /**
     * Creates a temporary file asynchronously
     */
    private static Mono<Path> createTempFileAsync(String prefix, String suffix) {
        return Mono.fromCallable(() -> Files.createTempFile(prefix, suffix))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Deletes a file asynchronously
     */
    private static Mono<Void> deleteFileAsync(Path path) {
        return Mono.fromCallable(() -> {
                    Files.deleteIfExists(path);
                    return true;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
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
            Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(resource, new DefaultDataBufferFactory(), BUFFER_SIZE);

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