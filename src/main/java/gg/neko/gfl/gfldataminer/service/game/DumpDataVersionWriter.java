package gg.neko.gfl.gfldataminer.service.game;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.DumpDataVersion;
import gg.neko.gfl.gfldataminer.service.json.JsonMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

@Component
@AllArgsConstructor
public class DumpDataVersionWriter {

    private final FileConfig fileConfig;
    private final JsonMapper jsonMapper;

    public File writeDumpDataVersion(List<DumpDataVersion> dataVersions) {
        Path dumpDataVersionPath = Paths.get(fileConfig.getDumpDataVersionPath());
        File dumpDataVersionFile = dumpDataVersionPath.toFile();
        return jsonMapper.toJsonFile(dumpDataVersionFile, dataVersions.stream().sorted(Comparator.comparing(DumpDataVersion::getRegion)).toList());
    }

}
