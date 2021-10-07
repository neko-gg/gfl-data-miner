package gg.neko.gfl.gfldataminer.service.file;

import gg.neko.gfl.gfldataminer.service.reactor.BlockingWrapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;

@Slf4j
@Component
@AllArgsConstructor
public class ZipFileExtractor {

    private final BlockingWrapper blockingWrapper;

    public Mono<File> extractZipFile(File source, File destination) {
        log.info("extracting .zip file {} into {}", source, destination);
        return blockingWrapper.wrapBlockingCall(() -> this.blockingExtractZipFile(source, destination));
    }

    @SneakyThrows
    private File blockingExtractZipFile(File source, File destination) {
        new ZipFile(source).extractAll(destination.getAbsolutePath());
        return destination;
    }

}
