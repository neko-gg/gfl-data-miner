package gg.neko.gfl.gfldataminer.service;

import gg.neko.gfl.gfldataminer.config.StringSubstitutorConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.DumpDataVersion;
import lombok.AllArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@AllArgsConstructor
public class StringInterpolator {

    private final StringSubstitutorConfig stringSubstitutorConfig;

    public String interpolate(ClientInfo clientInfo, String source) {
        return StringSubstitutor.replace(source,
                                         Collections.singletonMap(stringSubstitutorConfig.getValues().getRegion(), clientInfo.getRegion()),
                                         stringSubstitutorConfig.getPrefix(),
                                         stringSubstitutorConfig.getSuffix());
    }

    public String interpolate(DumpDataVersion dataVersion, String source) {
        return StringSubstitutor.replace(source,
                                         Map.of(stringSubstitutorConfig.getValues().getRegion(),
                                                dataVersion.getRegion(),
                                                stringSubstitutorConfig.getValues().getClientVersion(),
                                                dataVersion.getClientVersion(),
                                                stringSubstitutorConfig.getValues().getAbVersion(),
                                                dataVersion.getAbVersion(),
                                                stringSubstitutorConfig.getValues().getDataVersion(),
                                                dataVersion.getDataVersion()),
                                         stringSubstitutorConfig.getPrefix(),
                                         stringSubstitutorConfig.getSuffix());
    }

}
