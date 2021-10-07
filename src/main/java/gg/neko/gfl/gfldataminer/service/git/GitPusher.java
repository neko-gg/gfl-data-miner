package gg.neko.gfl.gfldataminer.service.git;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.config.GitConfig;
import gg.neko.gfl.gfldataminer.model.DumpDataVersion;
import gg.neko.gfl.gfldataminer.service.StringInterpolator;
import gg.neko.gfl.gfldataminer.service.reactor.SchedulerProvider;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
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
    private final SchedulerProvider schedulerProvider;
    private final StringInterpolator stringInterpolator;

    public Mono<File> pushToGitRepo(List<DumpDataVersion> dataVersions) {
        File directory = Paths.get(gitConfig.getDirectory()).toFile();
        File gitDirectory = Paths.get(gitConfig.getGitDirectory()).toFile();
        File dumpDataVersionFile = Paths.get(fileConfig.getDumpDataVersionPath()).toFile();

        return Mono.just(gitDirectory)
                   .map(this::openGitRepository)
                   .flatMap(git -> copyAddCommit(dataVersions, dumpDataVersionFile, directory, git))
                   .map(this::push)
                   .map(__ -> directory);
    }

    private Mono<Git> copyAddCommit(List<DumpDataVersion> dataVersions, File dumpDataVersionFile, File directory, Git git) {
        return Mono.just(copyFileToDirectory(dumpDataVersionFile, directory))
                   .map(__ -> dumpDataVersionFile.getName())
                   .map(dumpDataVersionFileName -> addAndCommit(git, dumpDataVersionFileName, gitConfig.getLatestVersionsCommitMessage()))
                   .mergeWith(Flux.fromIterable(dataVersions).flatMap(dataVersion -> copyAddCommitRegion(git, directory, dataVersion)))
                   .reduce((a, b) -> a || b)
                   .filter(anythingToPush -> anythingToPush)
                   .map(__ -> git)
                   .subscribeOn(schedulerProvider.singleScheduler());
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

    private Mono<Boolean> copyAddCommitRegion(Git git, File gitDirectory, DumpDataVersion dataVersion) {
        return Mono.just(Paths.get(fileConfig.getOutputPath(), dataVersion.getRegion()).toFile())
                   .map(clientDirectory -> copyDirectoryToDirectory(clientDirectory, gitDirectory))
                   .map(__ -> addAndCommit(git, dataVersion.getRegion(), stringInterpolator.interpolate(dataVersion, gitConfig.getRegionCommitMessage())));
    }

    @SneakyThrows
    private Git openGitRepository(File gitRepositoryPath) {
        return Git.open(gitRepositoryPath);
    }

    @SneakyThrows
    private boolean addAndCommit(Git git, String filePattern, String message) {
        if (isClean(git)) {
            log.info("no files to add for pattern \"{}\"", filePattern);
            return false;
        }

        git.add().addFilepattern(filePattern).call();

        git.commit()
           .setMessage(message)
           .setCommitter(gitConfig.getUsername(), gitConfig.getEmail())
           .setAuthor(gitConfig.getUsername(), gitConfig.getEmail())
           .call();

        return true;
    }

    @SneakyThrows
    private Git push(Git git) {
        log.info("pushing to git repository");
        git.push()
           .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitConfig.getToken(), StringUtils.EMPTY))
           .call();

        return git;
    }

    @SneakyThrows
    private boolean isClean(Git git) {
        return git.status().call().isClean();
    }

}
