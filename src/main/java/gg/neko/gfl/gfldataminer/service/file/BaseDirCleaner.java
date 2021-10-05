package gg.neko.gfl.gfldataminer.service.file;

import gg.neko.gfl.gfldataminer.config.ClientConfig;
import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.service.StringInterpolator;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;

@Component
@AllArgsConstructor
public class BaseDirCleaner {

    private final FileConfig fileConfig;
    private final ClientConfig clientConfig;
    private final StringInterpolator stringInterpolator;

    @SneakyThrows
    public void cleanBaseDir() {
        File baseDir = Paths.get(fileConfig.getBasePath()).toFile();
        FileUtils.forceMkdir(baseDir);
        FileUtils.cleanDirectory(baseDir);

        clientConfig.getClients().forEach(this::createStcDirs);
    }

    @SneakyThrows
    private void createStcDirs(ClientInfo clientInfo) {
        String interpolatedStcPath = stringInterpolator.interpolate(clientInfo, fileConfig.getStc().getOutputPath());
        File stcOutDir = Paths.get(interpolatedStcPath).toFile();
        FileUtils.forceMkdir(stcOutDir);

        String interpolatedCatchdataPath = stringInterpolator.interpolate(clientInfo, fileConfig.getStc().getCatchdataOutputPath());
        File catchdataOutDir = Paths.get(interpolatedCatchdataPath).toFile();
        FileUtils.forceMkdir(catchdataOutDir);
    }

}
