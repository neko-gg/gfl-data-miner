package gg.neko.gfl.gfldataminer.service.asset;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.ProcessedFileResult;
import gg.neko.gfl.gfldataminer.model.resdata.ResData;
import gg.neko.gfl.gfldataminer.service.crypt.XorDecrypter;
import gg.neko.gfl.gfldataminer.service.reactor.SchedulerProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

@Slf4j
@Component
public class AssetTextesAssetProcessor implements AssetProcessor {

    private final FileConfig fileConfig;
    private final XorDecrypter xorDecrypter;
    private final AssetProcessorHelper assetProcessorHelper;
    private final SchedulerProvider schedulerProvider;

    public AssetTextesAssetProcessor(FileConfig fileConfig, XorDecrypter xorDecrypter, AssetProcessorHelper assetProcessorHelper, SchedulerProvider schedulerProvider) {
        this.fileConfig = fileConfig;
        this.xorDecrypter = xorDecrypter;
        this.assetProcessorHelper = assetProcessorHelper;
        this.schedulerProvider = schedulerProvider;
    }

    @Override
    public Mono<ProcessedFileResult> processAsset(ClientInfo clientInfo, ResData resData) {
        FileConfig.AssetBundleFileConfig assetTextesConfig = fileConfig.getAsset().getAssetBundles().getAssetTextes().getAssetConfig();
        return assetProcessorHelper.processAssetForConfig(clientInfo, resData, assetTextesConfig)
                                   .flatMap(processedFileResult -> decryptLuaFiles(clientInfo, processedFileResult))
                                   .subscribeOn(schedulerProvider.defaultScheduler());
    }

    private Mono<ProcessedFileResult> decryptLuaFiles(ClientInfo clientInfo, ProcessedFileResult processedFileResult) {
        return Flux.fromIterable(processedFileResult.getOutputFiles())
                   .groupBy(file -> file.getName().endsWith(fileConfig.getAsset().getLua().getInputExtension()))
                   .flatMap(groupedLuaFlux -> processGroupedLua(clientInfo, groupedLuaFlux))
                   .collectList()
                   .map(outputFiles -> ProcessedFileResult.builder().inputFile(processedFileResult.getInputFile()).outputFiles(outputFiles).build())
                   .subscribeOn(schedulerProvider.defaultScheduler());
    }


    private Flux<File> processGroupedLua(ClientInfo clientInfo, GroupedFlux<Boolean, File> groupedLuaFlux) {
        Boolean isLua = groupedLuaFlux.key();
        if (isLua) return groupedLuaFlux.flatMap(luaFile -> decryptLuaFile(clientInfo, luaFile)).subscribeOn(schedulerProvider.defaultScheduler());
        return groupedLuaFlux.subscribeOn(schedulerProvider.defaultScheduler());
    }

    private Mono<File> decryptLuaFile(ClientInfo clientInfo, File luaFile) {
        log.info("[{}] decrypting .lua file {}", clientInfo.getRegion().toUpperCase(), luaFile);

        return Mono.just(readFileToByteArray(luaFile))
                   .map(luaBytes -> xorDecrypter.decrypt(luaBytes, getLuaKey()))
                   .map(luaBytes -> writeByteArrayToFile(luaFile, luaBytes))
                   .map(file -> StringUtils.substringBeforeLast(file.getName(), fileConfig.getAsset().getLua().getInputExtension()))
                   .map(fileName -> MessageFormat.format("{0}{1}", fileName, fileConfig.getAsset().getLua().getOutputExtension()))
                   .map(fileName -> Paths.get(luaFile.getParent(), fileName))
                   .map(Path::toFile)
                   .map(renamedFile -> moveFile(luaFile, renamedFile))
                   .subscribeOn(schedulerProvider.defaultScheduler());
    }

    @SneakyThrows
    private byte[] getLuaKey() {
        return fileConfig.getAsset().getLua().getEncryptionKey().getBytes(fileConfig.getCharset());
    }

    @SneakyThrows
    private byte[] readFileToByteArray(File file) {
        return FileUtils.readFileToByteArray(file);
    }

    @SneakyThrows
    private File writeByteArrayToFile(File file, byte[] bytes) {
        FileUtils.writeByteArrayToFile(file, bytes);
        return file;
    }

    @SneakyThrows
    private File moveFile(File sourceFile, File destinationFile) {
        FileUtils.moveFile(sourceFile, destinationFile);
        return destinationFile;
    }

}
