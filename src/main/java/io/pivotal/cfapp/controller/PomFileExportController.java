package io.pivotal.cfapp.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.JavaAppDetail;
import io.pivotal.cfapp.service.JavaAppDetailService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;


@Slf4j
@RestController
@ConditionalOnProperty(prefix = "java.artifacts.fetch", name= "mode", havingValue="unpack-pom-contents-in-droplet")
public class PomFileExportController {

    private JavaAppDetailService service;

    @Autowired
    public PomFileExportController(JavaAppDetailService service) {
        this.service = service;
    }

    @GetMapping("/download/pomfiles")
    public ResponseEntity<Flux<DataBuffer>> downloadDependenciesTarball() {
        String filename =
            String.format("%s-%s.tar.gz", "java-application-maven-pom-files", DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss").format(LocalDateTime.now()));
        Flux<DataBuffer> tarball =
            service
                .findAll()
                .collectList()
                .flatMapMany(list -> createTarGz(list));
        return
            ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(tarball);
    }

    private static Flux<DataBuffer> createTarGz(List<JavaAppDetail> appDetails) {
        Flux<DataBuffer> result = Flux.empty();
        try (
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            TarArchiveOutputStream tarOutput = new TarArchiveOutputStream(byteArrayOutputStream)) {
            for (JavaAppDetail appDetail : appDetails) {
                if (appDetail.getPomContents() != null && !appDetail.getPomContents().isEmpty()) {
                    String dirPath = String.format("%s/%s/%s", appDetail.getOrganization(), appDetail.getSpace(), appDetail.getAppName());
                    Path entryPath = Paths.get(dirPath, "pom.xml");
                    TarArchiveEntry entry = new TarArchiveEntry(entryPath.toString());
                    byte[] contentBytes = appDetail.getPomContents().getBytes();
                    entry.setSize(contentBytes.length);
                    tarOutput.putArchiveEntry(entry);
                    tarOutput.write(contentBytes);
                    tarOutput.closeArchiveEntry();
                }
            }
            tarOutput.finish();
            byte[] tarBytes = byteArrayOutputStream.toByteArray();
            DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
            DataBuffer db = factory.wrap(tarBytes);
            result = Flux.just(db);
        } catch (IOException ioe) {
            log.error("Could not create Flux<DataBuffer>", ioe);
        }
        return result;
    }
}
