package gg.neko.gfl.gfldataminer.service.asset;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.service.StringInterpolator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@AllArgsConstructor
public class ResDataOutputFileResolver {

    private final FileConfig fileConfig;
    private final StringInterpolator stringInterpolator;

    public File resolveResDataOutputFile(ClientInfo clientInfo) {
        String interpolatedPath = stringInterpolator.interpolate(clientInfo, fileConfig.getAsset().getResDataResourcePath());
        Path destination = Paths.get(interpolatedPath);
        return destination.toFile();
    }

}
