package gg.neko.gfl.gfldataminer;

import gg.neko.gfl.gfldataminer.config.ClientConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.ProcessedFileResult;
import gg.neko.gfl.gfldataminer.service.file.BaseDirCleaner;
import gg.neko.gfl.gfldataminer.service.file.FileLister;
import gg.neko.gfl.gfldataminer.service.game.ClientVersionChecker;
import gg.neko.gfl.gfldataminer.service.reactor.SchedulerProvider;
import gg.neko.gfl.gfldataminer.service.stc.StcDownloader;
import gg.neko.gfl.gfldataminer.service.stc.StcFileExtractor;
import gg.neko.gfl.gfldataminer.service.stc.StcFileGroupProcessor;
import gg.neko.gfl.gfldataminer.service.stc.StcFileGrouper;
import gg.neko.gfl.gfldataminer.service.stc.StcFileSkipper;
import gg.neko.gfl.gfldataminer.service.web.LatestDataVersionRetriever;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

@Slf4j
@AllArgsConstructor
@SpringBootApplication
@ConfigurationPropertiesScan
public class GFLDataMinerApplication {

    private final BaseDirCleaner baseDirCleaner;
    private final ClientConfig clientConfig;
    private final LatestDataVersionRetriever latestDataVersionRetriever;
    private final ClientVersionChecker clientVersionChecker;
    private final StcDownloader stcDownloader;
    private final StcFileExtractor stcFileExtractor;
    private final SchedulerProvider schedulerProvider;
    private final FileLister fileLister;
    private final StcFileGrouper stcFileGrouper;
    private final StcFileGroupProcessor stcFileGroupProcessor;
    private final StcFileSkipper stcFileSkipper;

    public static void main(String[] args) {
        SpringApplication.run(GFLDataMinerApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            baseDirCleaner.cleanBaseDir();

            var x = Flux.fromIterable(clientConfig.getClients())
                        .flatMap(this::processClientInfo)
                        .subscribeOn(schedulerProvider.defaultScheduler())
                        .count()
                        .block();

            System.out.println(x);
        };
    }

    private Flux<ProcessedFileResult> processClientInfo(ClientInfo clientInfo) {
        return latestDataVersionRetriever.getLatestDataVersion(clientInfo)
                                         .flatMap(dataVersion -> clientVersionChecker.checkDataVersion(dataVersion, clientInfo))
                                         .flatMap(dataVersion -> stcDownloader.downloadStc(clientInfo, dataVersion))
                                         .flatMap(stcFile -> stcFileExtractor.extractStcFile(clientInfo, stcFile))
                                         .flatMapMany(fileLister::listFiles)
                                         .filter(stcFileSkipper::shouldNotSkipStcFile)
                                         //.take(1)
                                         .groupBy(stcFileGrouper)
                                         .flatMap(stcFileGroup -> stcFileGroupProcessor.processStcFileGroup(clientInfo, stcFileGroup));
                                         //.onErrorContinue((e, o) -> log.warn("{}", e.getMessage()));
    }

}
