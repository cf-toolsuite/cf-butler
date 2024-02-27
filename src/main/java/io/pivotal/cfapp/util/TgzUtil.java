package io.pivotal.cfapp.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TgzUtil {

    private static Logger log = LoggerFactory.getLogger(TgzUtil.class);

    public static Mono<String> findMatchingFiles(Flux<DataBuffer> dataBufferFlux, String fileExtension) {
        StringBuilder contentBuilder = new StringBuilder();
        return
            DataBufferUtils
                .join(dataBufferFlux)
                .flatMap(dataBuffer -> {
                    Mono<String> result = Mono.empty();
                    try (
                        InputStream is = dataBuffer.asInputStream(true); // true for releasing the buffer
                        CompressorInputStream cIs = new CompressorStreamFactory(true).createCompressorInputStream(is);
                        TarArchiveInputStream tarIs = new ArchiveStreamFactory(StandardCharsets.UTF_8.name()).createArchiveInputStream("tar", cIs);
                        )
                    {
                        TarArchiveEntry entry;
                        while ((entry = (TarArchiveEntry) tarIs.getNextEntry()) != null) {
                            if (!tarIs.canReadEntryData(entry)) {
                                continue;
                            }
                            if (entry.getName().endsWith(fileExtension)) {
                                Path tarEntryFilePath = Paths.get(entry.getName()).getFileName();
                                String tarEntryFilename = tarEntryFilePath != null ? tarEntryFilePath.toString(): "";
                                if (StringUtils.isNotBlank(tarEntryFilename)) {
                                    contentBuilder.append(tarEntryFilename + "\n");
                                }
                            }
                        }
                        result = Mono.just(contentBuilder.toString());
                    } catch (ArchiveException | CompressorException | IOException e) {
                        log.warn(String.format("Could not find embedded files matching %s", fileExtension), e);
                        result = Mono.empty();
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                    return result;
                });
    }

    public static Mono<String> extractFileContent(Flux<DataBuffer> dataBufferFlux, String filename) {
        return
            DataBufferUtils
                .join(dataBufferFlux)
                .flatMap(dataBuffer -> {
                    Mono<String> result = Mono.empty();
                    try (
                        InputStream is = dataBuffer.asInputStream(true); // true for releasing the buffer
                        CompressorInputStream cIs = new CompressorStreamFactory(true).createCompressorInputStream(is);
                        TarArchiveInputStream tarIs = new ArchiveStreamFactory(StandardCharsets.UTF_8.name()).createArchiveInputStream("tar", cIs);
                        )
                    {
                        TarArchiveEntry entry;
                        while ((entry = (TarArchiveEntry) tarIs.getNextEntry()) != null) {
                            if (!tarIs.canReadEntryData(entry)) {
                                continue;
                            }
                            if (entry.getName().endsWith(filename)) {
                                StringBuilder contentBuilder = new StringBuilder();
                                byte[] buffer = new byte[1024];
                                int len;
                                while ((len = tarIs.read(buffer)) != -1) {
                                    contentBuilder.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
                                }
                                result = Mono.just(contentBuilder.toString());
                                break;
                            }
                        }
                    } catch (ArchiveException | CompressorException | IOException e) {
                        log.warn(String.format("Could not extract %s file contents", filename), e);
                        result = Mono.empty();
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                    return result;
                });
    }

    public static void main(String[] args) {
        //File droplet = new File("/tmp/droplet_48a064f7-0de8-4c07-aacc-76c299c12509.tgz");
        File droplet = new File(args[0]);
        if (droplet.exists()) {
            try (FileInputStream fis = new FileInputStream(droplet);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                byte[] bytes = baos.toByteArray();
                DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
                DataBuffer db = factory.wrap(bytes);
                TgzUtil
                    .extractFileContent(Flux.just(db), "pom.xml")
                    .log()
                    .subscribe();
                TgzUtil
                    .findMatchingFiles(Flux.just(db), ".jar")
                    .log()
                    .subscribe();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
