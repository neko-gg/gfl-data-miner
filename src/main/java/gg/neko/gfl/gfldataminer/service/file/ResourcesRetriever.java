package gg.neko.gfl.gfldataminer.service.file;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ResourcesRetriever {

    private final ResourceLoader resourceLoader;

    @SneakyThrows
    public List<File> getResources(String resourcesPath) {
        return Arrays.stream(ResourcePatternUtils.getResourcePatternResolver(this.resourceLoader)
                                                 .getResources(resourcesPath))
                     .map(ResourcesRetriever::getFileFromResource)
                     .collect(Collectors.toList());
    }

    @SneakyThrows
    public File getResource(String resourcesPath) {
        return ResourceUtils.getFile(resourcesPath);
    }

    @SneakyThrows
    private static File getFileFromResource(Resource resource) {
        return resource.getFile();
    }

}
