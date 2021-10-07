package gg.neko.gfl.gfldataminer.model.resdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResData {

    private String resUrl;
    @JsonProperty("BaseAssetBundles")
    private List<AssetBundle> baseAssetBundles = new ArrayList<>();

}
