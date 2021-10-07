package gg.neko.gfl.gfldataminer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DumpDataVersion {
    private String region;
    private String dataVersion;
    private String abVersion;
    private String clientVersion;
}
