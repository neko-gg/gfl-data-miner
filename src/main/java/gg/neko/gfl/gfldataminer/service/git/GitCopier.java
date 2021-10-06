package gg.neko.gfl.gfldataminer.service.git;

import gg.neko.gfl.gfldataminer.config.ClientConfig;
import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.config.GitConfig;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class GitCopier {

    private final GitConfig gitConfig;
    private final FileConfig fileConfig;
    private final ClientConfig clientConfig;

    public Mono<File> copyToDataRepo() {
        return Mono.defer(this::doCopyToDataRepo);
    }

    private Mono<File> doCopyToDataRepo() {
        List<ClientInfo> clients = clientConfig.getClients();
        File gitDirectory = Paths.get(gitConfig.getDirectory()).toFile();
        File dumpDataVersionFile = Paths.get(fileConfig.getDumpDataVersionPath()).toFile();

        return Flux.fromIterable(clients)
                   .map(ClientInfo::getRegion)
                   .flatMap(region -> copyRegion(gitDirectory, region))
                   .collectList()
                   .map(__ -> copyFileToDirectory(dumpDataVersionFile, gitDirectory))
                   .map(__ -> gitDirectory);
    }

    private Mono<File> copyRegion(File gitDirectory, String region) {
        return Mono.just(Paths.get(fileConfig.getOutputPath(), region).toFile())
                   .map(clientDirectory -> copyDirectoryToDirectory(clientDirectory, gitDirectory));
    }

    @SneakyThrows
    private File copyDirectoryToDirectory(File source, File destination) {
        log.info("copying directory {} into git directory", source);
        FileUtils.deleteQuietly(new File(destination, source.getName()));
        FileUtils.copyDirectoryToDirectory(source, destination);
        return destination;
    }

    @SneakyThrows
    private File copyFileToDirectory(File source, File destination) {
        log.info("copying file {} into git directory", source);
        FileUtils.deleteQuietly(new File(destination, source.getName()));
        FileUtils.copyFileToDirectory(source, destination, true);
        return destination;
    }

}
