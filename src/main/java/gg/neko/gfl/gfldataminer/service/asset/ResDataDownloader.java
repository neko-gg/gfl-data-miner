package gg.neko.gfl.gfldataminer.service.asset;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.DataVersion;
import gg.neko.gfl.gfldataminer.service.StringInterpolator;
import gg.neko.gfl.gfldataminer.service.web.FileDownloader;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.function.Function;

@Slf4j
@Component
@AllArgsConstructor
public class ResDataDownloader {

    private final FileConfig fileConfig;
    private final FileDownloader fileDownloader;
    private final ResDataFileNameResolver resDataFileNameResolver;
    private final StringInterpolator stringInterpolator;

    public Mono<File> downloadResData(ClientInfo clientInfo, DataVersion dataVersion) {
        log.info("[{}] downloading res data file for {}", clientInfo.getRegion().toUpperCase(), dataVersion);
        String resDataFileName = resDataFileNameResolver.resolveResDataFileName(dataVersion);
        String interpolatedPath = stringInterpolator.interpolate(clientInfo, fileConfig.getAsset().getPath());
        Function<UriBuilder, URI> uriFunction = uriBuilder -> uriBuilder.path(resDataFileName).build();
        return fileDownloader.downloadFile(clientInfo.getAssetHost(), interpolatedPath, resDataFileName, uriFunction);
    }

}
