package gg.neko.gfl.gfldataminer.service.stc;

import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.service.reactor.BlockingWrapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;

@Component
@AllArgsConstructor
public class StcFileReader {

    private final StcMapper stcMapper;
    private final BlockingWrapper blockingWrapper;

    @SneakyThrows
    public Mono<StcFileStream> readStcFile(ClientInfo clientInfo, File file) {
        return blockingWrapper.wrapBlockingCall(() -> new StcFileStream(stcMapper, clientInfo, file));
    }

}
