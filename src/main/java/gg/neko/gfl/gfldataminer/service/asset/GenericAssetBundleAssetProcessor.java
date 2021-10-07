package gg.neko.gfl.gfldataminer.service.asset;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.ProcessedFileResult;
import gg.neko.gfl.gfldataminer.model.resdata.ResData;
import gg.neko.gfl.gfldataminer.service.reactor.SchedulerProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@AllArgsConstructor
public class GenericAssetBundleAssetProcessor implements AssetProcessor {

    private final FileConfig fileConfig;
    private final AssetProcessorHelper assetProcessorHelper;
    private final SchedulerProvider schedulerProvider;

    @Override
    public Flux<ProcessedFileResult> processAsset(ClientInfo clientInfo, ResData resData) {
        List<FileConfig.AssetBundleFileConfig> assetConfigs = fileConfig.getAsset().getAssetBundles().getGenericAssetBundle().getAssetConfigs();
        return Flux.fromIterable(assetConfigs)
                   .flatMap(assetConfig -> assetProcessorHelper.processAssetForConfig(clientInfo, resData, assetConfig))
                   .subscribeOn(schedulerProvider.defaultScheduler());
    }

}
