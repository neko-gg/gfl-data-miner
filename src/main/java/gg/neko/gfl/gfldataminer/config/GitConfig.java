package gg.neko.gfl.gfldataminer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("git")
public class GitConfig {

    private String remoteUrl;
    private String directory;
    private String gitDirectory;
    private String token;
    private String username;
    private String email;
    private String latestVersionsCommitMessage;
    private String regionCommitMessage;

}
