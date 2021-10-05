package gg.neko.gfl.gfldataminer.service.web;

import gg.neko.gfl.gfldataminer.config.WebConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.DataVersion;
import gg.neko.gfl.gfldataminer.service.json.JsonMapper;
import gg.neko.gfl.gfldataminer.service.reactor.SchedulerProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class LatestDataVersionRetriever {

    private final WebConfig webConfig;
    private final JsonMapper jsonMapper;
    private final SchedulerProvider schedulerProvider;

    public Mono<DataVersion> getLatestDataVersion(ClientInfo clientInfo) {
        return WebClient.builder()
                        .baseUrl(clientInfo.getGameHost())
                        .build()
                        .post()
                        .uri(uriBuilder -> uriBuilder.path(this.webConfig.getGame().getBasePath()).path(this.webConfig.getGame().getVersionPath()).build())
                        .retrieve()
                        .bodyToMono(String.class)
                        .publishOn(schedulerProvider.defaultScheduler())
                        .map(body -> jsonMapper.fromJsonString(body, DataVersion.class));
    }

}
