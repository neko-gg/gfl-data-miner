package gg.neko.gfl.gfldataminer.config;

import gg.neko.gfl.gfldataminer.model.ClientInfo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties("client")
public class ClientConfig {

    private List<ClientInfo> clients;

}
