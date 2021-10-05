package gg.neko.gfl.gfldataminer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataVersion {
    @JsonProperty("data_version")
    private String dataVersion;
    @JsonProperty("ab_version")
    private String abVersion;
    @JsonProperty("client_version")
    private String clientVersion;
}
