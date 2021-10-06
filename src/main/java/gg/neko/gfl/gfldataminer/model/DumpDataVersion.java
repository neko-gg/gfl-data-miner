package gg.neko.gfl.gfldataminer.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DumpDataVersion {
    private String region;
    private String dataVersion;
    private String abVersion;
    private String clientVersion;
}
