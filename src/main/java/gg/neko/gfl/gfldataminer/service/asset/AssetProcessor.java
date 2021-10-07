package gg.neko.gfl.gfldataminer.service.asset;

import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.ProcessedFileResult;
import gg.neko.gfl.gfldataminer.model.resdata.ResData;
import org.reactivestreams.Publisher;

public interface AssetProcessor {

    Publisher<ProcessedFileResult> processAsset(ClientInfo clientInfo, ResData resData);

}
