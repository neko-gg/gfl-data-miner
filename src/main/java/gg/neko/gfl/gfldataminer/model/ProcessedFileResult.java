package gg.neko.gfl.gfldataminer.model;

import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.util.List;

@Data
@Builder
public class ProcessedFileResult {
    private File inputFile;
    private List<File> outputFiles;
}
