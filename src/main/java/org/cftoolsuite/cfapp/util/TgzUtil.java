package org.cftoolsuite.cfapp.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TgzUtil {

    private static Logger log = LoggerFactory.getLogger(TgzUtil.class);

    public static Mono<String> findMatchingFiles(Flux<DataBuffer> incoming, String extension) {
        Mono<DataBuffer> fullContent =
            DataBufferUtils
                .join(incoming);
        Mono<InputStream> inputStreamMono = fullContent.map(dataBuffer ->
            dataBuffer.asInputStream(true)
        );
        return inputStreamMono.flatMapMany(is -> findMatchingFiles(is, extension))
                .collectList()
                .map(list -> String.join(System.getProperty("line.separator"), list));
    }

    private static Flux<String> findMatchingFiles(InputStream is, String extension) {
        try {
            CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(is);
            TarArchiveInputStream tarIs = new ArchiveStreamFactory().createArchiveInputStream("tar", cis);
            return Flux.create(sink -> {
                try {
                    TarArchiveEntry entry;
                    while ((entry = (TarArchiveEntry) tarIs.getNextEntry()) != null) {
                        if (!tarIs.canReadEntryData(entry)) {
                            continue;
                        }
                        if (!entry.isDirectory() && entry.getName().endsWith(extension)) {
                            Path tarEntryFilePath = Paths.get(entry.getName()).getFileName();
                            String tarEntryFilename = tarEntryFilePath != null ? tarEntryFilePath.toString() : "";
                            log.trace("Found {}", tarEntryFilename);
                            sink.next(tarEntryFilename);
                        }
                    }
                    sink.complete();
                } catch (IOException e) {
                    log.error(String.format("Could not process entry in TarArchiveInputStream with extension %s", extension), e);
                    sink.error(e);
                } finally {
                    try {
                        tarIs.close();
                    } catch (IOException e) {
                        log.error("Problem closing TarArchiveInputStream", e);
                    }
                }
            });
        } catch (ArchiveException | CompressorException e) {
            log.error("Problem creating TarArchiveInputStream", e);
            return Flux.error(e);
        }
    }

    public static Mono<String> extractFileContent(Flux<DataBuffer> incoming, String filename) {
        Mono<DataBuffer> fullContent =
            DataBufferUtils
                .join(incoming);
        Mono<InputStream> inputStreamMono = fullContent.map(dataBuffer ->
            dataBuffer.asInputStream(true)
        );
        return inputStreamMono.flatMapMany(is -> extractFileContent(is, filename))
                .collectList()
                .map(list -> String.join("", list));
    }

    private static Flux<String> extractFileContent(InputStream is, String filename) {
        try {
            CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(is);
            TarArchiveInputStream tarIs = new ArchiveStreamFactory().createArchiveInputStream("tar", cis);
            return Flux.create(sink -> {
                try {
                    TarArchiveEntry entry;
                    while ((entry = (TarArchiveEntry) tarIs.getNextEntry()) != null) {
                        if (!tarIs.canReadEntryData(entry)) {
                            continue;
                        }
                        if (!entry.isDirectory() && entry.getName().endsWith(filename)) {
                            log.trace("Found {}", filename);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = tarIs.read(buffer)) != -1) {
                                sink.next(new String(buffer, 0, len, StandardCharsets.UTF_8));
                            }
                        }
                    }
                    sink.complete();
                } catch (IOException e) {
                    log.error(String.format("Could not find entry in TarArchiveInputStream with filename %s", filename), e);
                    sink.error(e);
                } finally {
                    try {
                        tarIs.close();
                    } catch (IOException e) {
                        log.error("Problem closing TarArchiveInputStream", e);
                    }
                }
            });
        } catch (ArchiveException | CompressorException e) {
            log.error("Problem creating TarArchiveInputStream", e);
            return Flux.error(e);
        }
    }

    public static void main(String[] args) {
        //File droplet = new File("/tmp/droplet_2be29bc3-0e2c-40f9-b454-db89c15c723f.tgz");
        File droplet = new File(args[0]);
        IntStream.rangeClosed(1, 30).forEach(i -> {
            if (droplet.exists()) {
                try {
                    byte[] bytes = Files.readAllBytes(droplet.toPath());
                    DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
                    DataBuffer db1 = factory.wrap(bytes);
                    DataBuffer db2 = factory.wrap(bytes);
                    TgzUtil
                        .extractFileContent(Flux.just(db1), "pom.xml")
                        .log()
                        .subscribe();
                    TgzUtil
                        .findMatchingFiles(Flux.just(db2), ".jar")
                        .log()
                        .subscribe();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
