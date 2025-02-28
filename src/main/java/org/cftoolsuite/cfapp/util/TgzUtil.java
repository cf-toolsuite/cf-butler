package org.cftoolsuite.cfapp.util;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TgzUtil {

    private static Logger log = LoggerFactory.getLogger(TgzUtil.class);

    public static Mono<String> findMatchingFiles(Flux<DataBuffer> incoming, String extension) {
        return incoming.flatMap(buffer -> {
                    InputStream is = buffer.asInputStream(true);
                    try {
                        return findMatchingFiles(is, extension);
                    } catch (CompressorException | ArchiveException e) {
                        log.error("Problem creating TarArchiveInputStream", e);
                        return Flux.error(e);
                    }
                }).collectList()
                .map(list -> String.join(System.lineSeparator(), list));
    }

    private static Flux<String> findMatchingFiles(InputStream is, String extension) throws CompressorException, ArchiveException {
        try (CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(is);
             TarArchiveInputStream tarIs = new ArchiveStreamFactory().createArchiveInputStream("tar", cis)) {
            return Flux.create(sink -> {
                try {
                    TarArchiveEntry entry;
                    while ((entry = (TarArchiveEntry) tarIs.getNextEntry()) != null) {
                        if (!tarIs.canReadEntryData(entry)) {
                            continue;
                        }
                        if (!entry.isDirectory() && entry.getName().endsWith(extension)) {
                            String tarEntryFilename = Paths.get(entry.getName()).getFileName().toString();
                            log.trace("Found {}", tarEntryFilename);
                            sink.next(tarEntryFilename);
                        }
                    }
                    sink.complete();
                } catch (IOException e) {
                    log.error(String.format("Could not process entry in TarArchiveInputStream with extension %s", extension), e);
                    sink.error(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Mono<String> extractFileContent(Flux<DataBuffer> incoming, String filename) {
        return incoming.flatMap(buffer -> {
                    InputStream is = buffer.asInputStream(true);
                    try {
                        return extractFileContent(is, filename);
                    } catch (CompressorException | ArchiveException e) {
                        log.error("Problem creating TarArchiveInputStream", e);
                        return Flux.error(e);
                    }
                })
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString);
    }

    private static Flux<String> extractFileContent(InputStream is, String filename) throws CompressorException, ArchiveException {
        return Mono.fromCallable(() -> {
                    try (CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(is);
                         TarArchiveInputStream tarIs = new ArchiveStreamFactory().createArchiveInputStream("tar", cis)) {
                        return findContentInTarEntry(tarIs, filename);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromStream);
    }

    private static Stream<String> findContentInTarEntry(TarArchiveInputStream tarIs, String filename) throws IOException {
        TarArchiveEntry entry;
        while ((entry = tarIs.getNextEntry()) != null) {
            if (!tarIs.canReadEntryData(entry)) {
                continue;
            }
            if (!entry.isDirectory() && entry.getName().endsWith(filename)) {
                log.trace("Found {}", filename);
                byte[] buffer = new byte[1024];
                return Stream.generate(() -> {
                    try {
                        int bytesRead = tarIs.read(buffer);
                        if (bytesRead == -1) {
                            return null; // Signal the end of the stream
                        }
                        return new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).takeWhile(Objects::nonNull);
            }
        }
        return Stream.empty();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: TgzUtil <droplet_file>");
            System.exit(1);
        }
        java.io.File dropletFile = new java.io.File(args[0]);
        if (!dropletFile.exists()) {
            System.err.println("Droplet file not found: " + dropletFile.getAbsolutePath());
            System.exit(1);
        }

        PathResource resource = new PathResource(dropletFile.toPath());
        try {
            // Define the chunk size.
            int chunkSize = 8192;
            Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(resource, new DefaultDataBufferFactory(), chunkSize);
            Mono<String> pomContent = TgzUtil.extractFileContent(dataBufferFlux, "pom.xml").log();
            Mono<String> jarFilenames = TgzUtil.findMatchingFiles(dataBufferFlux.cache(), ".jar").log();

            Mono.zip(pomContent, jarFilenames)
                    .subscribe(
                            tuple -> {
                                log.info("pom.xml content:\n{}", tuple.getT1());
                                log.info("JAR filenames:\n{}", tuple.getT2());
                            },
                            error -> log.error("Error during processing", error),
                            () -> log.info("Processing complete")
                    );
        } catch (Exception e) {
            log.error("Error processing droplet file", e);
        }
    }
}