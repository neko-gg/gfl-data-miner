package gg.neko.gfl.gfldataminer.service.stc;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.config.WebConfig;
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
public class StcDownloader {

    private final WebConfig webConfig;
    private final FileConfig fileConfig;
    private final FileDownloader fileDownloader;
    private final StcFileNameResolver stcFileNameResolver;
    private final StringInterpolator stringInterpolator;

    public Mono<File> downloadStc(ClientInfo clientInfo, DataVersion dataVersion) {
        log.info("[{}] downloading stc file for {}", clientInfo.getRegion().toUpperCase(), dataVersion);
        String stcFileName = stcFileNameResolver.resolveStcFileName(dataVersion.getDataVersion());
        String interpolatedPath = stringInterpolator.interpolate(clientInfo, fileConfig.getStc().getPath());
        Function<UriBuilder, URI> uriFunction = uriBuilder -> uriBuilder.path(webConfig.getCdn().getBasePath()).path(stcFileName).build();
        return fileDownloader.downloadFile(clientInfo.getCdnHost(), interpolatedPath, stcFileName, uriFunction);
    }

}
