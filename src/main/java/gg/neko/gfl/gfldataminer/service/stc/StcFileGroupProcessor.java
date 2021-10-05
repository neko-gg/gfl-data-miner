package gg.neko.gfl.gfldataminer.service.stc;

import gg.neko.gfl.gfldataminer.enums.StcFileGroup;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.ProcessedFileResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;

import java.io.File;

@Slf4j
@Component
@AllArgsConstructor
public class StcFileGroupProcessor {

    private final StcFileProcessor stcFileProcessor;
    private final StcCatchdataProcessor stcCatchdataProcessor;

    public Flux<ProcessedFileResult> processStcFileGroup(ClientInfo clientInfo, GroupedFlux<StcFileGroup, File> stcFileGroupFlux) {
        StcFileGroup stcFileGroup = stcFileGroupFlux.key();

        if (stcFileGroup == StcFileGroup.STC_FILE) return stcFileGroupFlux.flatMap(stcFile -> stcFileProcessor.processStcFile(clientInfo, stcFile));
        if (stcFileGroup == StcFileGroup.CATCHDATA) return stcFileGroupFlux.flatMap(catchdataFile -> stcCatchdataProcessor.processStcCatchdata(clientInfo, catchdataFile));

        return stcFileGroupFlux.doOnNext(stcFile -> log.warn("[{}] ignoring unknown stc file type {}", clientInfo.getRegion().toUpperCase(), stcFile))
                               .thenMany(Flux.empty());
    }

}
