package gg.neko.gfl.gfldataminer.model.resdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetBundle {

    private String assetBundleName;
    @JsonProperty("resname")
    private String resName;

}
