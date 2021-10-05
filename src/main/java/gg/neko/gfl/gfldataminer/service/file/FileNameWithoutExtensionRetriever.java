package gg.neko.gfl.gfldataminer.service.file;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FileNameWithoutExtensionRetriever {

    public String getFileNameWithoutExtension(File file) {
        return FilenameUtils.removeExtension(file.getName());
    }

}
