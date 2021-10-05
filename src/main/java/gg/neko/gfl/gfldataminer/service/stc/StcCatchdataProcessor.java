package gg.neko.gfl.gfldataminer.service.stc;

import com.fasterxml.jackson.core.type.TypeReference;
import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.ProcessedFileResult;
import gg.neko.gfl.gfldataminer.service.crypt.XorDecrypter;
import gg.neko.gfl.gfldataminer.service.json.JsonMapper;
import gg.neko.gfl.gfldataminer.service.reactor.BlockingWrapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
@AllArgsConstructor
public class StcCatchdataProcessor {

    private final BlockingWrapper blockingWrapper;
    private final XorDecrypter xorDecrypter;
    private final FileConfig fileConfig;
    private final JsonMapper jsonMapper;
    private final StcCatchdataOutputFileResolver stcCatchdataOutputFileResolver;

    public Mono<ProcessedFileResult> processStcCatchdata(ClientInfo clientInfo, File catchdataFile) {
        return blockingWrapper.wrapBlockingCall(() -> readFileToByteArray(catchdataFile))
                              .map(this::decryptCatchdata)
                              .map(ByteArrayInputStream::new)
                              .map(this::newGZIPInputStream)
                              .map(this::readStreamToString)
                              .map(this::splitCatchdataString)
                              .flatMapMany(Flux::fromArray)
                              .filter(this::isCatchdataLineNotBlank)
                              .map(this::getCatchdataMapFromLine)
                              .flatMap(this::getCatchdataMapFlux)
                              .map(catchdataMapFlux -> writeCatchdataMapToJsonFile(clientInfo, catchdataMapFlux))
                              .collectList()
                              .map(outputFiles -> processStcCatchdataFileResult(catchdataFile, outputFiles));
    }

    @SneakyThrows
    private byte[] decryptCatchdata(byte[] data) {
        byte[] key = fileConfig.getStc().getCatchdataEncryptionKey().getBytes(fileConfig.getCharset());
        return xorDecrypter.decrypt(data, key);
    }

    @SneakyThrows
    private byte[] readFileToByteArray(File catchdataFile) {
        return FileUtils.readFileToByteArray(catchdataFile);
    }

    @SneakyThrows
    private GZIPInputStream newGZIPInputStream(InputStream inputStream) {
        return new GZIPInputStream(inputStream);
    }

    @SneakyThrows
    private String readStreamToString(InputStream inputStream) {
        return IOUtils.toString(inputStream, fileConfig.getCharset());
    }

    private String[] splitCatchdataString(String catchdataString) {
        return catchdataString.split(fileConfig.getStc().getCatchdataLineSeparator());
    }

    private boolean isCatchdataLineNotBlank(String catchdataLine) {
        return !catchdataLine.isBlank();
    }

    private Map<String, Object> getCatchdataMapFromLine(String catchdataLine) {
        return jsonMapper.fromJsonString(catchdataLine, new TypeReference<>() {});
    }

    private Flux<Map.Entry<String, Object>> getCatchdataMapFlux(Map<String, Object> catchdataMap) {
        return Flux.fromIterable(catchdataMap.entrySet());
    }

    private File writeCatchdataMapToJsonFile(ClientInfo clientInfo, Map.Entry<String, Object> catchdataMapEntry) {
        String catchdataMapKey = catchdataMapEntry.getKey();
        File outputFile = stcCatchdataOutputFileResolver.resolveCatchdataOutputFile(clientInfo, catchdataMapKey);
        log.info("[{}] writing parsed {} catchdata entry into {}", clientInfo.getRegion().toUpperCase(), catchdataMapKey, outputFile);
        return jsonMapper.toJsonFile(outputFile, catchdataMapEntry.getValue());
    }

    private ProcessedFileResult processStcCatchdataFileResult(File catchdataFile, List<File> outputFiles) {
        return ProcessedFileResult.builder()
                                  .inputFile(catchdataFile)
                                  .outputFiles(outputFiles)
                                  .build();
    }

}
