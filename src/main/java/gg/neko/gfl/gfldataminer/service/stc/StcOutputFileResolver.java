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
public class StcOutputFileResolver {

    private final FileConfig fileConfig;
    private final StcMapper stcMapper;
    private final StringInterpolator stringInterpolator;


    public File resolveOutputFile(ClientInfo clientInfo, File stcFile) {
        String stcName = stcMapper.getStcMapping(clientInfo, stcFile).getName();
        String outputFileName = MessageFormat.format("{0}{1}",
                                                     stcName,
                                                     fileConfig.getJson().getExtension());
        String interpolatedPath = stringInterpolator.interpolate(clientInfo, fileConfig.getStc().getOutputPath());
        Path destination = Paths.get(interpolatedPath, outputFileName);
        return destination.toFile();
    }

}
