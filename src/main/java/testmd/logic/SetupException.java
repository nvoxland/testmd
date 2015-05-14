package testmd.logic;

public class SetupException extends RuntimeException {

    public SetupException(String message) {
        super(message);
    }

    public SetupException(String message, Throwable cause) {
        super(message, cause);
    }

    public SetupException(Throwable cause) {
        super(cause);
    }

    public SetupException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
