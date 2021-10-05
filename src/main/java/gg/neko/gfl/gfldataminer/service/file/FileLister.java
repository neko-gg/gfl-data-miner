package gg.neko.gfl.gfldataminer.service.file;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.Objects;

@Component
public class FileLister {


    public Flux<File> listFiles(File file) {
        return Flux.fromArray(Objects.requireNonNull(file.listFiles()));
    }

}
