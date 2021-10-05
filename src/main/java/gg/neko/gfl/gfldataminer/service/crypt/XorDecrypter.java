package gg.neko.gfl.gfldataminer.service.crypt;

import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

@Component
public class XorDecrypter {

    public byte[] decrypt(byte[] data, byte[] key) {
        byte[] decrypted = new byte[data.length];
        IntStream.range(0, data.length).forEach(i -> decrypted[i] = (byte) (data[i] ^ key[i % key.length]));
        return decrypted;
    }

}
