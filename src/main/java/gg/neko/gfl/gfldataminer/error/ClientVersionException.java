package gg.neko.gfl.gfldataminer.error;

public class ClientVersionException extends RuntimeException {

    public ClientVersionException(String message) {
        super(message);
    }

    public ClientVersionException(String message, Throwable cause) {
        super(message, cause);
    }
}
