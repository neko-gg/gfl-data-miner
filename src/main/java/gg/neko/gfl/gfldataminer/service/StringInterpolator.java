package gg.neko.gfl.gfldataminer.service;

import gg.neko.gfl.gfldataminer.config.StringSubstitutorConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import lombok.AllArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;

import java.util.Collections;

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

}
