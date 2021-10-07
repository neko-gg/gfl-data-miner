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
    private PythonFileConfig python;
    private StcFileConfig stc;
    private AssetFileConfig asset;
    private String dumpDataVersionPath;

    @Data
    public static class JsonFileConfig {
        private String extension;
    }

    @Data
    public static class PythonFileConfig {
        private String executable;
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

    @Data
    public static class AssetFileConfig {
        private String key;
        private String iv;
        private String assetExtractorPath;
        private String assetExtractorResourcePath;
        private String path;
        private String outputPath;
        private String resDataResourcePath;
        private String resDataExtractedPath;
        private String resDataOutputPath;
        private String extension;
        private LuaFileConfig lua;
        private AssetBundlesFileConfig assetBundles;
    }

    @Data
    public static class LuaFileConfig {
        private String inputExtension;
        private String outputExtension;
        private String encryptionKey;
    }

    @Data
    public static class AssetBundlesFileConfig {
        GenericAssetBundlesFileConfig genericAssetBundle;
        private AssetTextesFileConfig assetTextes;
    }

    @Data
    public static class GenericAssetBundlesFileConfig {
        private List<AssetBundleFileConfig> assetConfigs;
    }

    @Data
    public static class AssetTextesFileConfig {
        private AssetBundleFileConfig assetConfig;
    }

    @Data
    public static class AssetBundleFileConfig {
        private String assetName;
        private String assetPath;
        private String assetExtractedName;
        private String assetResourcesPath;
        private String assetOutputPath;
    }

}
