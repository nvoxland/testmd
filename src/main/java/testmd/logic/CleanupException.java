package testmd.logic;

public class CleanupException extends RuntimeException {

    public CleanupException() {
    }

    public CleanupException(String message) {
        super(message);
    }

    public CleanupException(String message, Throwable cause) {
        super(message, cause);
    }

    public CleanupException(Throwable cause) {
        super(cause);
    }
}
