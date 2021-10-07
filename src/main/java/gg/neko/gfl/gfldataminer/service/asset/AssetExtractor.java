package gg.neko.gfl.gfldataminer.service.asset;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.service.file.ResourcesRetriever;
import gg.neko.gfl.gfldataminer.service.reactor.BlockingWrapper;
import gg.neko.gfl.gfldataminer.service.reactor.SchedulerProvider;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Paths;

@Slf4j
@Component
@AllArgsConstructor
public class AssetExtractor {

    private final FileConfig fileConfig;
    private final ResourcesRetriever resourcesRetriever;
    private final BlockingWrapper blockingWrapper;
    private final SchedulerProvider schedulerProvider;

    @SneakyThrows
    public void setupAssetExtractor() {
        File assetExtractorResource = resourcesRetriever.getResource(fileConfig.getAsset().getAssetExtractorResourcePath());
        File assetExtractorOutput = Paths.get(fileConfig.getAsset().getAssetExtractorPath()).toFile();
        FileUtils.copyFile(assetExtractorResource, assetExtractorOutput);
    }

    public Mono<File> extract(ClientInfo clientInfo, File inputFile, File outputDirectory) {
        log.info("[{}] extracting asset {} into {}", clientInfo.getRegion().toUpperCase(), inputFile, outputDirectory);
        String assetExtractorPath = fileConfig.getAsset().getAssetExtractorPath();
        String assetExtractorAbsolutePath = Paths.get(assetExtractorPath).toFile().getAbsolutePath();

        CommandLine commandLine = new CommandLine(fileConfig.getPython().getExecutable());
        commandLine.addArgument(StringUtils.quoteArgument(assetExtractorAbsolutePath));
        commandLine.addArgument(StringUtils.quoteArgument(inputFile.getAbsolutePath()));
        commandLine.addArgument(StringUtils.quoteArgument(outputDirectory.getAbsolutePath()));
        DefaultExecutor executor = new DefaultExecutor();
        return blockingWrapper.wrapBlockingCall(() -> execute(commandLine, executor))
                              .map(__ -> outputDirectory)
                              .subscribeOn(schedulerProvider.defaultScheduler());
    }

    @SneakyThrows
    private int execute(CommandLine commandLine, DefaultExecutor executor) {
        return executor.execute(commandLine);
    }

}
