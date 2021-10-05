package gg.neko.gfl.gfldataminer.service.stc;

import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.ProcessedFileResult;
import gg.neko.gfl.gfldataminer.service.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class StcFileProcessor {

    private final StcFileReader stcFileReader;
    private final JsonMapper jsonMapper;
    private final StcOutputFileResolver stcOutputFileResolver;

    public Mono<ProcessedFileResult> processStcFile(ClientInfo clientInfo, File stcFile) {
        return stcFileReader.readStcFile(clientInfo, stcFile)
                            .map(StcFileStream::parseStcFile)
                            .flatMap(Flux::collectList)
                            .map(parsedStc -> writeParsedStcToJsonFile(clientInfo, stcFile, parsedStc))
                            .map(outputFile -> processStcFileResult(stcFile, outputFile));
    }

    private File writeParsedStcToJsonFile(ClientInfo clientInfo, File stcFile, List<Map<String, Object>> parsedStc) {
        File outputFile = stcOutputFileResolver.resolveOutputFile(clientInfo, stcFile);
        log.info("[{}] writing parsed {} stc file into {}", clientInfo.getRegion().toUpperCase(), stcFile, outputFile);
        return jsonMapper.toJsonFile(outputFile, parsedStc);
    }

    private ProcessedFileResult processStcFileResult(File stcFile, File outputFile) {
        return ProcessedFileResult.builder()
                                  .inputFile(stcFile)
                                  .outputFiles(Collections.singletonList(outputFile))
                                  .build();
    }

}
