package gg.neko.gfl.gfldataminer.service.file;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.service.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.Charset;

@Component
@AllArgsConstructor
public class JsonFileReader {

    private final JsonMapper jsonMapper;
    private final FileConfig fileConfig;

    @SneakyThrows
    public <T> T readJsonFile(File file, Class<T> valueType) {
        Charset charset = Charset.forName(this.fileConfig.getCharset());
        String jsonFile = FileUtils.readFileToString(file, charset);
        return jsonMapper.fromJsonString(jsonFile, valueType);
    }

}
