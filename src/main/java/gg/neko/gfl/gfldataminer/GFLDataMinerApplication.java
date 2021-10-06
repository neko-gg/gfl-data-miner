package gg.neko.gfl.gfldataminer;

import gg.neko.gfl.gfldataminer.config.ClientConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.DataVersion;
import gg.neko.gfl.gfldataminer.model.DumpDataVersion;
import gg.neko.gfl.gfldataminer.service.file.BaseDirCleaner;
import gg.neko.gfl.gfldataminer.service.file.FileLister;
import gg.neko.gfl.gfldataminer.service.game.ClientVersionChecker;
import gg.neko.gfl.gfldataminer.service.game.DumpDataVersionWriter;
import gg.neko.gfl.gfldataminer.service.git.GitCloner;
import gg.neko.gfl.gfldataminer.service.git.GitCopier;
import gg.neko.gfl.gfldataminer.service.git.GitPusher;
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
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

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
    private final DumpDataVersionWriter dumpDataVersionWriter;
    private final GitCloner gitCloner;
    private final GitCopier gitCopier;
    private final GitPusher gitPusher;

    public static void main(String[] args) {
        SpringApplication.run(GFLDataMinerApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            baseDirCleaner.cleanBaseDir();
            List<ClientInfo> clients = clientConfig.getClients();

            gitCloner.cloneDataRepo()
                     .zipWith(Flux.fromIterable(clients)
                                  .flatMap(this::getLatestVersionAndProcessClientData)
                                  .collectSortedList(Comparator.comparing(DumpDataVersion::getRegion))
                                  .flatMap(dataVersions -> Mono.just(dumpDataVersionWriter.writeDumpDataVersion(dataVersions))
                                                               .map(__ -> dataVersions)),
                              (repository, dataVersion) -> dataVersion)
                     .flatMap(dataVersions -> gitCopier.copyToDataRepo()
                                                       .flatMap(__ -> gitPusher.pushToGitRepo(dataVersions)))
                     .subscribeOn(schedulerProvider.defaultScheduler())
                     .block();
        };
    }

    private Mono<DumpDataVersion> getLatestVersionAndProcessClientData(ClientInfo clientInfo) {
        return latestDataVersionRetriever.getLatestDataVersion(clientInfo)
                                         .flatMap(dataVersion -> processClientData(clientInfo, dataVersion))
                                         .subscribeOn(schedulerProvider.defaultScheduler());
    }

    private Mono<DumpDataVersion> processClientData(ClientInfo clientInfo, DataVersion latestDataVersion) {
        return Mono.just(latestDataVersion)
                   .flatMap(dataVersion -> clientVersionChecker.checkDataVersion(dataVersion, clientInfo))
                   .flatMap(dataVersion -> stcDownloader.downloadStc(clientInfo, dataVersion))
                   .flatMap(stcFile -> stcFileExtractor.extractStcFile(clientInfo, stcFile))
                   .flatMapMany(fileLister::listFiles)
                   .filter(stcFileSkipper::shouldNotSkipStcFile)
                   .groupBy(stcFileGrouper)
                   .flatMap(stcFileGroup -> stcFileGroupProcessor.processStcFileGroup(clientInfo, stcFileGroup))
                   .collectList()
                   .map(__ -> DumpDataVersion.builder()
                                             .region(clientInfo.getRegion())
                                             .dataVersion(latestDataVersion.getDataVersion())
                                             .abVersion(latestDataVersion.getAbVersion())
                                             .clientVersion(latestDataVersion.getClientVersion())
                                             .build())
                   .subscribeOn(schedulerProvider.defaultScheduler());
    }

}
