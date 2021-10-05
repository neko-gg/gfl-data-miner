package gg.neko.gfl.gfldataminer.service.stc;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.service.StringInterpolator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

@Component
@AllArgsConstructor
public class StcCatchdataOutputFileResolver {

    private final FileConfig fileConfig;
    private final StringInterpolator stringInterpolator;

    public File resolveCatchdataOutputFile(ClientInfo clientInfo, String catchdataMapKey) {
        String outputFileName = MessageFormat.format("{0}{1}",
                                                     catchdataMapKey,
                                                     fileConfig.getJson().getExtension());
        String interpolatedPath = stringInterpolator.interpolate(clientInfo, fileConfig.getStc().getCatchdataOutputPath());
        Path destination = Paths.get(interpolatedPath, outputFileName);
        return destination.toFile();
    }

}
