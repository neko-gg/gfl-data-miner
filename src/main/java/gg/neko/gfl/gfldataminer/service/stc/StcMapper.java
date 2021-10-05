package gg.neko.gfl.gfldataminer.service.stc;

import gg.neko.gfl.gfldataminer.config.ClientConfig;
import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.StcMapping;
import gg.neko.gfl.gfldataminer.service.StringInterpolator;
import gg.neko.gfl.gfldataminer.service.file.FileNameWithoutExtensionRetriever;
import gg.neko.gfl.gfldataminer.service.file.JsonFileReader;
import gg.neko.gfl.gfldataminer.service.file.ResourcesRetriever;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StcMapper {

    private final JsonFileReader jsonFileReader;
    private final FileNameWithoutExtensionRetriever fileNameWithoutExtensionRetriever;

    private final Map<Pair<ClientInfo, String>, StcMapping> stcMappingMap;

    @SneakyThrows
    public StcMapper(JsonFileReader jsonFileReader, FileNameWithoutExtensionRetriever fileNameWithoutExtensionRetriever, ClientConfig clientConfig, StringInterpolator stringInterpolator, FileConfig fileConfig, ResourcesRetriever resourcesRetriever) {
        this.jsonFileReader = jsonFileReader;
        this.fileNameWithoutExtensionRetriever = fileNameWithoutExtensionRetriever;

        this.stcMappingMap = clientConfig.getClients()
                                         .stream()
                                         .flatMap(clientInfo -> resourcesRetriever.getResources(stringInterpolator.interpolate(clientInfo, fileConfig.getStc().getMappingPath()))
                                                                                  .stream()
                                                                                  .map(resource -> ImmutablePair.of(ImmutablePair.of(clientInfo,
                                                                                                                                     fileNameWithoutExtensionRetriever.getFileNameWithoutExtension(resource)),
                                                                                                                    readFileToStcMapping(resource))))
                                         .collect(Collectors.toUnmodifiableMap(Pair::getLeft, Pair::getRight));
    }

    public StcMapping getStcMapping(ClientInfo clientInfo, int id) {
        return getStcMapping(clientInfo, String.valueOf(id));
    }

    public StcMapping getStcMapping(ClientInfo clientInfo, File file) {
        return getStcMapping(clientInfo, fileNameWithoutExtensionRetriever.getFileNameWithoutExtension(file));
    }

    public StcMapping getStcMapping(ClientInfo clientInfo, String id) {
        return stcMappingMap.get(ImmutablePair.of(clientInfo, id));
    }

    private StcMapping readFileToStcMapping(File file) {
        return jsonFileReader.readJsonFile(file, StcMapping.class);
    }

}
