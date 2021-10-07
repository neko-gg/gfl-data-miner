package gg.neko.gfl.gfldataminer.service.asset;

import gg.neko.gfl.gfldataminer.config.FileConfig;
import gg.neko.gfl.gfldataminer.model.DataVersion;
import gg.neko.gfl.gfldataminer.service.crypt.DesEncrypter;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Base64;

@Component
@AllArgsConstructor
public class ResDataFileNameResolver {

    private static final String RES_DATA_TO_ENCRYPT_PATTERN = "{0}_{1}_AndroidResConfigData";
    private static final String RES_DATA_FILENAME_PATTERN = "{0}.txt";

    private final FileConfig fileConfig;
    private final DesEncrypter desEncrypter;

    @SneakyThrows
    public String resolveResDataFileName(DataVersion dataVersion) {
        String shortClientVersion = String.valueOf(Math.round(Integer.parseInt(dataVersion.getClientVersion()) / 100d) * 10);
        String resDataNameToEncrypt = MessageFormat.format(RES_DATA_TO_ENCRYPT_PATTERN, shortClientVersion, dataVersion.getAbVersion());
        byte[] resDataNameToEncryptBytes = resDataNameToEncrypt.getBytes(fileConfig.getCharset());

        byte[] key = Base64.getDecoder().decode(fileConfig.getAsset().getKey());
        byte[] iv = Base64.getDecoder().decode(fileConfig.getAsset().getIv());

        byte[] ivChopped = new byte[8];
        System.arraycopy(iv, 0, ivChopped, 0, 8);

        byte[] encryptedRedDataNameBytes = desEncrypter.encrypt(resDataNameToEncryptBytes, key, ivChopped);

        byte[] encryptedRedDataNameBase64 =  Base64.getEncoder().encode(encryptedRedDataNameBytes);
        String encryptedResDataName = new String(encryptedRedDataNameBase64, fileConfig.getCharset());
        String resDataName = encryptedResDataName.replaceAll("[^a-zA-Z0-9]", "");

        return MessageFormat.format(RES_DATA_FILENAME_PATTERN, resDataName);
    }

}
