package gg.neko.gfl.gfldataminer.service.asset;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.resdata.ResData;
import gg.neko.gfl.gfldataminer.service.StringInterpolator;
import gg.neko.gfl.gfldataminer.service.json.JsonMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;

@Component
@AllArgsConstructor
public class ResDataReader {

    private final FileConfig fileConfig;
    private final JsonMapper jsonMapper;
    private final StringInterpolator stringInterpolator;

    public ResData readResData(ClientInfo clientInfo) {
        String interpolatedPath = stringInterpolator.interpolate(clientInfo, fileConfig.getAsset().getResDataExtractedPath());
        File resDataFile = Paths.get(interpolatedPath).toFile();
        return jsonMapper.fromJsonFile(resDataFile, ResData.class);
    }

}
