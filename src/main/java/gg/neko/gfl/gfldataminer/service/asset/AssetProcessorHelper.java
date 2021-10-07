package gg.neko.gfl.gfldataminer.service.asset;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.ProcessedFileResult;
import gg.neko.gfl.gfldataminer.model.resdata.ResData;
import gg.neko.gfl.gfldataminer.service.StringInterpolator;
import gg.neko.gfl.gfldataminer.service.file.ZipFileExtractor;
import gg.neko.gfl.gfldataminer.service.reactor.SchedulerProvider;
import gg.neko.gfl.gfldataminer.service.web.FileDownloader;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

@Slf4j
@Component
@AllArgsConstructor
public class AssetProcessorHelper {

    private final FileConfig fileConfig;
    private final FileDownloader fileDownloader;
    private final AssetExtractor assetExtractor;
    private final ZipFileExtractor zipFileExtractor;
    private final StringInterpolator stringInterpolator;
    private final SchedulerProvider schedulerProvider;

    public Mono<ProcessedFileResult> processAssetForConfig(ClientInfo clientInfo, ResData resData, FileConfig.AssetBundleFileConfig assetBundleFileConfig) {
        log.info("[{}] processing {} asset bundle", clientInfo.getRegion().toUpperCase(), assetBundleFileConfig.getAssetName());

        return Flux.fromIterable(resData.getBaseAssetBundles())
                   .filter(assetBundle -> assetBundleFileConfig.getAssetName().equals(assetBundle.getAssetBundleName()))
                   .next()
                   .flatMap(assetBundle -> processAsset(clientInfo, assetBundle.getResName(), resData.getResUrl(), assetBundleFileConfig))
                   .subscribeOn(schedulerProvider.defaultScheduler());
    }

    private Mono<ProcessedFileResult> processAsset(ClientInfo clientInfo, String assetResourceName, String resourcesUrl, FileConfig.AssetBundleFileConfig assetBundleFileConfig) {
        log.info("[{}] downloading {} asset bundle", clientInfo.getRegion().toUpperCase(), assetBundleFileConfig.getAssetName());

        String interpolatedPath = stringInterpolator.interpolate(clientInfo, assetBundleFileConfig.getAssetPath());
        File outputDirectory = Paths.get(interpolatedPath).toFile();

        return Mono.just(MessageFormat.format("{0}{1}", assetResourceName, fileConfig.getAsset().getExtension()))
                   .flatMap(resFileName -> fileDownloader.downloadFile(resourcesUrl, interpolatedPath, resFileName, uriBuilder -> uriBuilder.path(resFileName).build()))
                   .flatMap(downloadedFile -> zipFileExtractor.extractZipFile(downloadedFile, outputDirectory))
                   .flatMap(downloadedFile -> processZippedAsset(clientInfo, downloadedFile, outputDirectory, assetBundleFileConfig))
                   .subscribeOn(schedulerProvider.defaultScheduler());
    }

    private Mono<ProcessedFileResult> processZippedAsset(ClientInfo clientInfo, File zippedAssetFile, File outputDirectory, FileConfig.AssetBundleFileConfig assetBundleFileConfig) {
        log.info("[{}] extracting {} asset bundle into {}", clientInfo.getRegion().toUpperCase(), assetBundleFileConfig.getAssetName(), outputDirectory);

        return Mono.just(zippedAssetFile)
                   .map(File::getAbsolutePath)
                   .map(directoryPath -> Paths.get(directoryPath, assetBundleFileConfig.getAssetExtractedName()))
                   .map(Path::toFile)
                   .flatMap(assetFile -> assetExtractor.extract(clientInfo, assetFile, outputDirectory))
                   .map(__ -> stringInterpolator.interpolate(clientInfo, assetBundleFileConfig.getAssetResourcesPath()))
                   .map(Paths::get)
                   .map(Path::toFile)
                   .map(resourcesDirectory -> moveDirectoryContentToDirectory(resourcesDirectory, getAssetOutputPath(clientInfo, assetBundleFileConfig)))
                   .flatMapMany(assetOutputDirectory -> Flux.fromIterable(FileUtils.listFiles(assetOutputDirectory, null, true)))
                   .collectList()
                   .map(outputFiles -> ProcessedFileResult.builder().inputFile(zippedAssetFile).outputFiles(outputFiles).build())
                   .subscribeOn(schedulerProvider.defaultScheduler());
    }

    private File getAssetOutputPath(ClientInfo clientInfo, FileConfig.AssetBundleFileConfig assetBundleFileConfig) {
        String assetOutputPath = stringInterpolator.interpolate(clientInfo, assetBundleFileConfig.getAssetOutputPath());
        return Paths.get(assetOutputPath).toFile();
    }

    @SneakyThrows
    private File moveDirectoryContentToDirectory(File sourceDirectory, File destinationDirectory) {
        FileUtils.listFiles(sourceDirectory, FileFileFilter.INSTANCE, null)
                 .forEach(file -> moveFileToDirectory(file, destinationDirectory));

        FileUtils.listFiles(sourceDirectory, DirectoryFileFilter.INSTANCE, null)
                 .forEach(directory -> moveDirectoryToDirectory(directory, destinationDirectory));

        return destinationDirectory;
    }

    @SneakyThrows
    private void moveFileToDirectory(File sourceFile, File destinationDirectory) {
        FileUtils.moveFileToDirectory(sourceFile, destinationDirectory, true);
    }

    @SneakyThrows
    private void moveDirectoryToDirectory(File sourceDirectory, File destinationDirectory) {
        FileUtils.moveDirectoryToDirectory(sourceDirectory, destinationDirectory, true);
    }

}
