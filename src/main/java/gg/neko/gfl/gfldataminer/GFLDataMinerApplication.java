package gg.neko.gfl.gfldataminer;

import gg.neko.gfl.gfldataminer.config.ClientConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.DataVersion;
import gg.neko.gfl.gfldataminer.model.DumpDataVersion;
import gg.neko.gfl.gfldataminer.model.ProcessedFileResult;
import gg.neko.gfl.gfldataminer.service.asset.AssetExtractor;
import gg.neko.gfl.gfldataminer.service.asset.AssetProcessor;
import gg.neko.gfl.gfldataminer.service.asset.ResDataDownloader;
import gg.neko.gfl.gfldataminer.service.asset.ResDataOutputFileResolver;
import gg.neko.gfl.gfldataminer.service.asset.ResDataReader;
import gg.neko.gfl.gfldataminer.service.asset.ResDataWriter;
import gg.neko.gfl.gfldataminer.service.file.BaseDirCleaner;
import gg.neko.gfl.gfldataminer.service.file.FileLister;
import gg.neko.gfl.gfldataminer.service.game.ClientVersionChecker;
import gg.neko.gfl.gfldataminer.service.game.DumpDataVersionWriter;
import gg.neko.gfl.gfldataminer.service.git.GitCloner;
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
    private final GitPusher gitPusher;
    private final ResDataDownloader resDataDownloader;
    private final AssetExtractor assetExtractor;
    private final ResDataOutputFileResolver resDataOutputFileResolver;
    private final ResDataReader resDataReader;
    private final ResDataWriter resDataWriter;
    private final List<AssetProcessor> assetProcessors;

    public static void main(String[] args) {
        SpringApplication.run(GFLDataMinerApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            baseDirCleaner.cleanBaseDir();
            assetExtractor.setupAssetExtractor();

            gitCloner.cloneDataRepo()
                     .zipWith(processClientInfos(), (repository, allAndChangedDataVersions) -> allAndChangedDataVersions)
                     .flatMap(gitPusher::pushToGitRepo)
                     .subscribeOn(schedulerProvider.defaultScheduler())
                     .block();
        };
    }

    private Mono<List<DumpDataVersion>> processClientInfos() {
        Flux<ClientInfo> clientInfoFlux = Flux.fromIterable(clientConfig.getClients()).subscribeOn(schedulerProvider.defaultScheduler()).cache();

        return clientInfoFlux.flatMap(this::processClientInfo)
                             .collectList()
                             .flatMap(this::writeDumpDataVersion)
                             .subscribeOn(schedulerProvider.defaultScheduler());
    }

    private Mono<DumpDataVersion> processClientInfo(ClientInfo clientInfo) {
        return getLatestDataVersion(clientInfo).flatMap(dataVersion -> clientVersionChecker.checkDataVersion(clientInfo, dataVersion))
                                               .flatMap(dataVersion -> processClientData(clientInfo, dataVersion))
                                               .subscribeOn(schedulerProvider.defaultScheduler());
    }

    private Mono<DumpDataVersion> processClientData(ClientInfo clientInfo, DataVersion dataVersion) {
        Flux<ProcessedFileResult> stcFlux = processStc(clientInfo, dataVersion);
        Flux<ProcessedFileResult> assetsFlux = processAssets(clientInfo, dataVersion);

        return stcFlux.mergeWith(assetsFlux)
                      .collectList()
                      .map(__ -> buildDumpDataVersion(clientInfo, dataVersion))
                      .subscribeOn(schedulerProvider.defaultScheduler());
    }

    private Flux<ProcessedFileResult> processStc(ClientInfo clientInfo, DataVersion dataVersion) {
        return stcDownloader.downloadStc(clientInfo, dataVersion)
                            .flatMap(stcFile -> stcFileExtractor.extractStcFile(clientInfo, stcFile))
                            .flatMapMany(fileLister::listFiles)
                            .filter(stcFileSkipper::shouldNotSkipStcFile)
                            .groupBy(stcFileGrouper)
                            .flatMap(stcFileGroup -> stcFileGroupProcessor.processStcFileGroup(clientInfo, stcFileGroup))
                            .subscribeOn(schedulerProvider.defaultScheduler());
    }

    private Flux<ProcessedFileResult> processAssets(ClientInfo clientInfo, DataVersion dataVersion) {
        return resDataDownloader.downloadResData(clientInfo, dataVersion)
                                .flatMap(resDataFile -> assetExtractor.extract(clientInfo, resDataFile, resDataOutputFileResolver.resolveResDataOutputFile(clientInfo)))
                                .map(__ -> resDataWriter.writeResData(clientInfo))
                                .map(__ -> resDataReader.readResData(clientInfo))
                                .flatMapMany(resData -> Flux.fromIterable(assetProcessors).flatMap(assetProcessor -> assetProcessor.processAsset(clientInfo, resData)).subscribeOn(schedulerProvider.defaultScheduler()))
                                .subscribeOn(schedulerProvider.defaultScheduler());
    }

    private Mono<DataVersion> getLatestDataVersion(ClientInfo clientInfo) {
        return latestDataVersionRetriever.getLatestDataVersion(clientInfo).cache();
    }

    private Mono<List<DumpDataVersion>> writeDumpDataVersion(List<DumpDataVersion> dumpDataVersions) {
        return Mono.just(dumpDataVersionWriter.writeDumpDataVersion(dumpDataVersions))
                   .map(__ -> dumpDataVersions);
    }

    private DumpDataVersion buildDumpDataVersion(ClientInfo clientInfo, DataVersion latestDataVersion) {
        return DumpDataVersion.builder()
                              .region(clientInfo.getRegion())
                              .dataVersion(latestDataVersion.getDataVersion())
                              .abVersion(latestDataVersion.getAbVersion())
                              .clientVersion(latestDataVersion.getClientVersion())
                              .build();
    }

}
