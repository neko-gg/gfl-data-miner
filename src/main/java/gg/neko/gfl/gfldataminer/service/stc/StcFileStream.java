package gg.neko.gfl.gfldataminer.service.stc;

import gg.neko.gfl.gfldataminer.error.FileException;
import gg.neko.gfl.gfldataminer.model.ClientInfo;
import gg.neko.gfl.gfldataminer.model.StcMapping;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.model.enums.RandomAccessFileMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class StcFileStream {

    private final File file;

    private final RandomAccessFile randomAccessFile;
    private final StcMapping stcMapping;
    private final long totalRows;
    private final long totalCols;

    public StcFileStream(StcMapper stcMapper, ClientInfo clientInfo, File file) {
        try {
            this.file = file;
            this.randomAccessFile = new RandomAccessFile(file, RandomAccessFileMode.READ.getValue());

            this.randomAccessFile.seek(4);
            this.totalRows = readUnsignedShort();
            this.totalCols = readUnsignedByte();

            this.stcMapping = stcMapper.getStcMapping(clientInfo, file);
            log.info("[{}] reading stc file {}; rows: {}, cols: {}", clientInfo.getRegion().toUpperCase(), file.getName(), totalRows, totalCols);
        } catch (Exception e) {
            throw new FileException(MessageFormat.format("failed to read stc file {0}", file), e);
        }
    }

    public Flux<Map<String, Object>> parseStcFile() {
        if (totalRows == 0) {
            return Flux.empty();
        }

        List<Integer> columnsCodes = parseColumnCodes();

        seekToData();
        return Flux.range(0, Math.toIntExact(this.totalRows))
                   .map(r -> columnsCodes)
                   .map(this::columnCodesToStcValues)
                   .flatMapSequential(this::stcValuesToMap);
    }

    private List<Integer> parseColumnCodes() {
        return IntStream.range(0, Math.toIntExact(this.totalCols))
                        .map(c -> Math.toIntExact(readUnsignedByte()))
                        .boxed()
                        .collect(Collectors.toList());
    }

    private void seekToData() {
        this.skipBytes(4);
        long dataStartOffset = this.readInt();
        this.seek(dataStartOffset);
    }

    private Object readNextStcValue(int columnCode) {
        switch (columnCode) {
            case 1:
                return this.readUnsignedByte();
            case 5:
                return this.readInt();
            case 8:
                return this.readLong();
            case 9:
                return this.readFloat();
            case 11:
                return this.readUTF();
        }

        throw new FileException(MessageFormat.format("failed to read stc value, unknown column code {0} in file {1}", columnCode, this.file));
    }

    private Flux<Object> columnCodesToStcValues(List<Integer> codes) {
        return Flux.fromIterable(codes).map(this::readNextStcValue);
    }

    private Mono<Map<String, Object>> stcValuesToMap(Flux<Object> values) {
        return values.index().collectMap(t -> this.stcMapping.getFields().get(Math.toIntExact(t.getT1())), Tuple2::getT2, LinkedHashMap::new);
    }

    @SneakyThrows
    private long readUnsignedByte() {
        return readPaddedAsLong(1);
    }

    @SneakyThrows
    private long readUnsignedShort() {
        return readPaddedAsLong(2);
    }

    @SneakyThrows
    private long readInt() {
        byte[] bytes = new byte[4];
        this.randomAccessFile.read(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    @SneakyThrows
    private long readLong() {
        return readPaddedAsLong(8);
    }

    @SneakyThrows
    private long readPaddedAsLong(int n) {
        byte[] bytes = new byte[8];
        this.randomAccessFile.read(bytes, 0, n);
        IntStream.range(n, 8).forEach(i -> bytes[i] = 0);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    @SneakyThrows
    private float readFloat() {
        byte[] bytes = new byte[4];
        this.randomAccessFile.read(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    @SneakyThrows
    private String readUTF() {
        skipBytes(1);
        long length = readUnsignedShort();
        byte[] bytes = new byte[Math.toIntExact(length)];
        this.randomAccessFile.read(bytes);
        return new String(bytes);
    }

    @SneakyThrows
    private void skipBytes(int n) {
        int skippedBytes = this.randomAccessFile.skipBytes(n);
        if (skippedBytes != n) throw new FileException(MessageFormat.format("failed to skip {0} bytes while reading {1}", n, this.file));
    }

    @SneakyThrows
    private void seek(long pos) {
        this.randomAccessFile.seek(pos);
    }

}
