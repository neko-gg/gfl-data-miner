package gg.neko.gfl.gfldataminer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("string-substitutor")
public class StringSubstitutorConfig {

    private String prefix;
    private String suffix;
    private StringSubstitutorValuesConfig values;

    @Data
    public static class StringSubstitutorValuesConfig {
        private String region;
        private String clientVersion;
        private String abVersion;
        private String dataVersion;
    }

}
