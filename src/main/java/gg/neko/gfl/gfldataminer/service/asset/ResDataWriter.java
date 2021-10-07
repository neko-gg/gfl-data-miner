package gg.neko.gfl.gfldataminer.service.asset;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.service.StringInterpolator;
import gg.neko.gfl.gfldataminer.service.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;

@Component
@AllArgsConstructor
public class ResDataWriter {

    private final FileConfig fileConfig;
    private final JsonMapper jsonMapper;
    private final StringInterpolator stringInterpolator;

    @SneakyThrows
    public File writeResData(ClientInfo clientInfo) {
        String interpolatedInputPath = stringInterpolator.interpolate(clientInfo, fileConfig.getAsset().getResDataExtractedPath());
        String interpolatedOutputPath = stringInterpolator.interpolate(clientInfo, fileConfig.getAsset().getResDataOutputPath());
        File interpolatedInputFile = Paths.get(interpolatedInputPath).toFile();
        File interpolatedOutputFile = Paths.get(interpolatedOutputPath).toFile();

        FileUtils.copyFile(interpolatedInputFile, interpolatedOutputFile);
        return interpolatedOutputFile;
    }

}
