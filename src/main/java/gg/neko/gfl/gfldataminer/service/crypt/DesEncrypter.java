package gg.neko.gfl.gfldataminer.service.crypt;

import lombok.SneakyThrows;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.springframework.stereotype.Component;

@Component
public class DesEncrypter {

    @SneakyThrows
    public byte[] encrypt(byte[] data, byte[] key, byte[] iv) {
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new DESEngine()));
        cipher.init(true, new ParametersWithIV(new KeyParameter(key), iv));

        byte[] cipherText = new byte[cipher.getOutputSize(data.length)];
        int processedBytes = cipher.processBytes(data, 0, data.length, cipherText, 0);
        cipher.doFinal(cipherText, processedBytes);

        return cipherText;
    }

}
