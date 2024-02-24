package io.pivotal.cfapp.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TgzUtil {

    public static Mono<String> extractFileContent(Flux<DataBuffer> dataBufferFlux, String filename) {
        return
            DataBufferUtils
                .join(dataBufferFlux)
                .flatMap(dataBuffer -> {
                    Mono<String> result = Mono.empty();
                    try (
                        InputStream inputStream = dataBuffer.asInputStream(true); // true for releasing the buffer
                        GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(inputStream);
                        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipInputStream)
                        )
                    {
                        TarArchiveEntry entry;
                        while ((entry = (TarArchiveEntry) tarArchiveInputStream.getNextEntry()) != null) {
                            if (entry.getName().endsWith(filename)) {
                                StringBuilder contentBuilder = new StringBuilder();
                                byte[] buffer = new byte[1024];
                                int len;
                                while ((len = tarArchiveInputStream.read(buffer)) != -1) {
                                    contentBuilder.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
                                }
                                result = Mono.just(contentBuilder.toString());
                                break;
                            }
                        }
                    } catch (IOException ioe) {
                        result = Mono.error(ioe);
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                    return result;
                });
    }

    public static void main(String[] args) {
        //File droplet = new File("/tmp/droplet_bc11a63f-36ed-423f-9c4b-eb57f0b0cd40.tgz");
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
