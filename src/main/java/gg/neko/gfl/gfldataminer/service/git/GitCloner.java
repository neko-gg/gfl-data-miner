package gg.neko.gfl.gfldataminer.service.git;

import gg.neko.gfl.gfldataminer.config.GitConfig;
import gg.neko.gfl.gfldataminer.service.reactor.BlockingWrapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Paths;

@Slf4j
@Component
@AllArgsConstructor
public class GitCloner {

    private final GitConfig gitConfig;
    private final BlockingWrapper blockingWrapper;

    @SneakyThrows
    public Mono<File> cloneDataRepo() {
        return blockingWrapper.wrapBlockingCall(this::blockingCloneDataRepo);
    }

    @SneakyThrows
    public File blockingCloneDataRepo() {
        File directory = Paths.get(gitConfig.getDirectory()).toFile();

        log.info("cloning git repository from {} into {}", gitConfig.getRemoteUrl(), directory);

        Git.cloneRepository()
           .setURI(gitConfig.getRemoteUrl())
           .setDirectory(directory)
           .call();

        log.info("git repository cloned in {}", directory);

        return directory;
    }

}
