package io.pivotal.cfapp.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
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
public class JavaAppDetailController {

    private JavaAppDetailService service;

    @Autowired
    public JavaAppDetailController(JavaAppDetailService service) {
        this.service = service;
    }

    @GetMapping("/download/pomfiles")
    public ResponseEntity<Flux<DataBuffer>> downloadDependenciesTarball() {
        String filename =
            String.format("%s-%s.tgz", "java-application-maven-pom-files", DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now()));
        Flux<DataBuffer> tarball =
            service
                .findAll()
                .collectList()
                .flatMapMany(list -> createTarGz(list, filename));
        return
            ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(tarball);
    }

    private static Flux<DataBuffer> createTarGz(List<JavaAppDetail> appDetails, String outputFileName) {
        Flux<DataBuffer> result = Flux.empty();
        try {
            Path tarFilePath = Files.createTempFile("apps", ".tar");
            try (TarArchiveOutputStream tarOs =
                new TarArchiveOutputStream(
                    new GzipCompressorOutputStream(new BufferedOutputStream(new FileOutputStream(tarFilePath.toFile()))))) {
                for (JavaAppDetail appDetail : appDetails) {
                    String dirPath = String.format("%s/%s/%s", appDetail.getOrganization(), appDetail.getSpace(), appDetail.getAppName());
                    Path entryPath = Paths.get(dirPath, "pom.xml");
                    TarArchiveEntry entry = new TarArchiveEntry(entryPath.toString());
                    byte[] contentBytes = appDetail.getPomContents().getBytes();
                    entry.setSize(contentBytes.length);
                    tarOs.putArchiveEntry(entry);
                    tarOs.write(contentBytes);
                    tarOs.closeArchiveEntry();
                }
                tarOs.finish();

                File tarball = new File(outputFileName);
                Files.move(tarFilePath, tarball.toPath());

                try (FileInputStream fis = new FileInputStream(tarball);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    byte[] bytes = baos.toByteArray();
                    DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
                    DataBuffer db = factory.wrap(bytes);
                    result = Flux.just(db);
                } catch (IOException ioe) {
                    log.error("Could not create Flux<DataBuffer>", ioe);
                }
            } catch (IOException ioe) {
                log.error("Could not create TAR output stream", ioe);
            }
        } catch (IOException ioe) {
            log.error("Could not create temporary file named app.tar", ioe);
        }
        return result;
    }
}
