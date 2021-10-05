package gg.neko.gfl.gfldataminer.service.stc;

import gg.neko.gfl.gfldataminer.enums.StcFileGroup;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.function.Function;

@Component
public class StcFileGrouper implements Function<File, StcFileGroup> {

    @Override
    public StcFileGroup apply(File file) {
        String fileName = file.getName();
        if (fileName.endsWith(".stc")) return StcFileGroup.STC_FILE;
        if (fileName.equals("catchdata.dat")) return StcFileGroup.CATCHDATA;
        return StcFileGroup.UNKNOWN;
    }

}
