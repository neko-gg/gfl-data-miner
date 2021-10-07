package gg.neko.gfl.gfldataminer.service.game;

import gg.neko.gfl.gfldataminer.error.ClientVersionException;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.DataVersion;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;

@Component
public class ClientVersionChecker {

    public Mono<DataVersion> checkDataVersion(ClientInfo clientInfo, DataVersion dataVersion) {
        if (clientInfo.getDumpVersion().compareTo(dataVersion.getClientVersion()) >= 0) {
            return Mono.just(dataVersion);
        }

        return Mono.error(() -> new ClientVersionException(MessageFormat.format("[{0}] client dump version is less than remote client version, {1} < {2}; consider dumping and disassembling the game again",
                                                                                clientInfo.getRegion().toUpperCase(),
                                                                                clientInfo.getDumpVersion(),
                                                                                dataVersion.getClientVersion())));
    }

}
