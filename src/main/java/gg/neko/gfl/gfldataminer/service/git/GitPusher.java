package gg.neko.gfl.gfldataminer.service.git;

import gg.neko.gfl.gfldataminer.config.ClientConfig;
import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.config.GitConfig;
import gg.neko.gfl.gfldataminer.model.DumpDataVersion;
import gg.neko.gfl.gfldataminer.service.StringInterpolator;
import gg.neko.gfl.gfldataminer.service.reactor.SchedulerProvider;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class GitPusher {

    private final GitConfig gitConfig;
    private final FileConfig fileConfig;
    private final ClientConfig clientConfig;
    private final SchedulerProvider schedulerProvider;
    private final StringInterpolator stringInterpolator;

    public Mono<File> pushToGitRepo(List<DumpDataVersion> dataVersions) {
        File directory = Paths.get(gitConfig.getDirectory()).toFile();
        File gitDirectory = Paths.get(gitConfig.getGitDirectory()).toFile();
        File dumpDataVersionFile = Paths.get(fileConfig.getDumpDataVersionPath()).toFile();

        return Mono.just(gitDirectory)
                   .map(this::openGitRepository)
                   .flatMap(git -> addAndCommitRegions(dataVersions, dumpDataVersionFile, git))
                   .map(this::push)
                   .map(__ -> directory);
    }

    private Mono<Git> addAndCommitRegions(List<DumpDataVersion> dataVersions, File dumpDataVersionFile, Git git) {
        return Mono.just(dumpDataVersionFile.getName())
                   .map(dumpDataVersionFileName -> addAndCommit(git, dumpDataVersionFileName, "update latest versions"))
                   .flatMapMany(__ -> Flux.fromIterable(dataVersions))
                   .map(dataVersion -> addAndCommit(git, dataVersion.getRegion(), stringInterpolator.interpolate(dataVersion, gitConfig.getRegionCommitMessage())))
                   .collectList()
                   .map(__ -> git)
                   .subscribeOn(schedulerProvider.singleScheduler());
    }

    @SneakyThrows
    private Git openGitRepository(File gitRepositoryPath) {
        return Git.open(gitRepositoryPath);
    }

    @SneakyThrows
    private Git addAndCommit(Git git, String filePattern, String message) {
        git.add().addFilepattern(filePattern).call();
        git.commit()
           .setMessage(message)
           .setCommitter(gitConfig.getUsername(), gitConfig.getEmail())
           .setAuthor(gitConfig.getUsername(), gitConfig.getEmail())
           .call();
        return git;
    }

    @SneakyThrows
    private Iterable<PushResult> push(Git git) {
        log.info("pushing to git repository");

        return git.push()
                  .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitConfig.getToken(), StringUtils.EMPTY))
                  .call();
    }

}
