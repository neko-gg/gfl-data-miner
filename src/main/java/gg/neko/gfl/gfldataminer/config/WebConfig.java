package gg.neko.gfl.gfldataminer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("web")
public class WebConfig {

    private WebGameConfig game;
    private WebCdnConfig cdn;

    @Data
    public static class WebGameConfig {
        private String basePath;
        private String versionPath;
    }

    @Data
    public static class WebCdnConfig {
        private String basePath;
    }
}
