package gg.neko.gfl.gfldataminer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


@Data
@ConfigurationProperties("file")
public class FileConfig {

    private String charset;
    private String basePath;
    private String outputPath;
    private JsonFileConfig json;
    private StcFileConfig stc;
    private String dumpDataVersionPath;

    @Data
    public static class JsonFileConfig {
        private String extension;
    }

    @Data
    public static class StcFileConfig {
        private String path;
        private String extractPath;
        private String outputPath;
        private String catchdataEncryptionKey;
        private String catchdataLineSeparator;
        private String catchdataOutputPath;
        private String mappingPath;
        private List<String> skip;
    }

}
