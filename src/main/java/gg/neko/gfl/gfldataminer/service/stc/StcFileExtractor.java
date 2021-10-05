package gg.neko.gfl.gfldataminer.service.stc;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.service.StringInterpolator;
import gg.neko.gfl.gfldataminer.service.file.ZipFileExtractor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Paths;

@Slf4j
@Component
@AllArgsConstructor
public class StcFileExtractor {

    private final FileConfig fileConfig;
    private final ZipFileExtractor zipFileExtractor;
    private final StringInterpolator stringInterpolator;

    public Mono<File> extractStcFile(ClientInfo clientInfo, File stcFile) {
        log.info("[{}] extracting stc file {}", clientInfo.getRegion().toUpperCase(), stcFile);
        String interpolatedPath = stringInterpolator.interpolate(clientInfo, this.fileConfig.getStc().getExtractPath());
        File destination = Paths.get(interpolatedPath).toFile();
        return zipFileExtractor.extractZipFile(stcFile, destination);
    }

}
