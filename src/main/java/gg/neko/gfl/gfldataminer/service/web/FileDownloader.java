package gg.neko.gfl.gfldataminer.service.web;

import gg.neko.gfl.gfldataminer.config.WebConfig;
import gg.neko.gfl.gfldataminer.error.FileException;
import gg.neko.gfl.gfldataminer.service.reactor.SchedulerProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.function.Function;

@Slf4j
@Component
@AllArgsConstructor
public class FileDownloader {

    private final SchedulerProvider schedulerProvider;
    private final WebConfig webConfig;

    public Mono<File> downloadFile(WebClient webClient, Function<UriBuilder, URI> uriFunction, Path destination) {
        log.info("downloading file in {}", destination);
        Flux<DataBuffer> dataBufferFlux = webClient.get()
                                                   .uri(uriFunction)
                                                   .retrieve()
                                                   .bodyToFlux(DataBuffer.class);

        File parentDestinationFile = destination.getParent().toFile();
        boolean destinationParentDirsCreated = parentDestinationFile.mkdirs();
        if (!destinationParentDirsCreated && !parentDestinationFile.exists()) return Mono.error(new FileException(MessageFormat.format("Failed to create parent dirs before downloading file: {0}", destination.toAbsolutePath())));

        return DataBufferUtils.write(dataBufferFlux,
                                     destination,
                                     StandardOpenOption.TRUNCATE_EXISTING,
                                     StandardOpenOption.CREATE)
                              .publishOn(schedulerProvider.defaultScheduler())
                              .then(Mono.just(destination))
                              .map(Path::toFile);
    }

    public Mono<File> downloadFile(String host, String path, String filename, Function<UriBuilder, URI> uriFunction) {
        Path destination = Paths.get(path, filename);

        WebClient webClient = WebClient.builder()
                                       .baseUrl(host)
                                       .build();

        return downloadFile(webClient, uriFunction, destination);
    }

}
