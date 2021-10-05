package gg.neko.gfl.gfldataminer.model;

import lombok.Data;

@Data
public class ClientInfo {
    private String region;
    private String gameHost;
    private String cdnHost;
    private String dumpVersion;
}
