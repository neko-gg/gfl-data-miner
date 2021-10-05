package gg.neko.gfl.gfldataminer.service.stc;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
public class StcFileNameResolver {

    private static final String STC_FILENAME_PATTERN = "/stc_{0}{1}.zip";

    public String resolveStcFileName(String dataVersion) {
        return MessageFormat.format(STC_FILENAME_PATTERN, dataVersion, DigestUtils.md5Hex(dataVersion));
    }

}
