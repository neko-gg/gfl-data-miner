package gg.neko.gfl.gfldataminer.service.web;

import com.fasterxml.jackson.core.type.TypeReference;
import gg.neko.gfl.gfldataminer.config.WebConfig;
import gg.neko.gfl.gfldataminer.model.DumpDataVersion;
import gg.neko.gfl.gfldataminer.service.json.JsonMapper;
import gg.neko.gfl.gfldataminer.service.reactor.SchedulerProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@AllArgsConstructor
public class LatestDumpDataVersionRetriever {

    private final WebConfig webConfig;
    private final JsonMapper jsonMapper;
    private final SchedulerProvider schedulerProvider;

    public Mono<List<DumpDataVersion>> getLatestDumpDataVersion() {
        return WebClient.builder()
                        .baseUrl(webConfig.getLatestVersionsUrl())
                        .build()
                        .get()
                        .retrieve()
                        .bodyToMono(String.class)
                        .publishOn(schedulerProvider.defaultScheduler())
                        .map(body -> jsonMapper.fromJsonString(body, new TypeReference<>() {}));
    }

}
