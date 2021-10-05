package gg.neko.gfl.gfldataminer.service.stc;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@AllArgsConstructor
public class StcFileSkipper {

    private final FileConfig fileConfig;

    @SneakyThrows
    public boolean shouldNotSkipStcFile(File file) {
        return fileConfig.getStc()
                         .getSkip()
                         .stream()
                         .map(Paths::get)
                         .map(Path::toFile)
                         .map(File::getName)
                         .noneMatch(fileName -> fileName.equals(file.getName()));
    }

}
