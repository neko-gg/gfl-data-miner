package gg.neko.gfl.gfldataminer.service.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@AllArgsConstructor
public class JsonMapper {

    private final ObjectMapper objectMapper;
    private final JsonPrettyPrinter jsonPrettyPrinter;

    @SneakyThrows
    public <T> T fromJsonString(String content, Class<T> valueType) {
        return this.objectMapper.readValue(content, valueType);
    }

    @SneakyThrows
    public <T> T fromJsonString(String content, TypeReference<T> valueTypeRef) {
        return this.objectMapper.readValue(content, valueTypeRef);
    }

    @SneakyThrows
    public String toJsonString(Object value) {
        return objectMapper.writer(jsonPrettyPrinter.getPrettyPrinter()).writeValueAsString(value);
    }

    @SneakyThrows
    public File toJsonFile(File file, Object value) {
        objectMapper.writer(jsonPrettyPrinter.getPrettyPrinter()).writeValue(file, value);
        return file;
    }

}
