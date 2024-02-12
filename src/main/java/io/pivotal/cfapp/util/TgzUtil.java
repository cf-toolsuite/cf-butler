package io.pivotal.cfapp.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TgzUtil {

    public static Mono<String> extractFileContent(Flux<DataBuffer> dataBufferFlux, String filename) {
        return DataBufferUtils.join(dataBufferFlux)
            .flatMap(dataBuffer -> {
                try {
                    InputStream is = dataBuffer.asInputStream(true); // true for releasing the buffer
                    return extractFromTarGz(is, filename);
                } finally {
                    DataBufferUtils.release(dataBuffer);
                }
            });
    }

    private static Mono<String> extractFromTarGz(InputStream inputStream, String filename) {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipInputStream)) {

            TarArchiveEntry entry;
            String content;
            while ((entry = (TarArchiveEntry) tarArchiveInputStream.getNextEntry()) != null) {
                if (entry.getName().endsWith(filename)) {
                    content = new BufferedReader(new InputStreamReader(tarArchiveInputStream, StandardCharsets.UTF_8))
                            .lines().collect(Collectors.joining("\n"));
                    return Mono.just(content);
                }
            }
        } catch (IOException e) {
            return Mono.error(e);
        }
        return Mono.empty();
    }

    public static void main(String[] args) {
        //File droplet = new File("/tmp/droplet_00b473e8-eedf-4243-be14-4dce6cdd88f7.tgz");
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
