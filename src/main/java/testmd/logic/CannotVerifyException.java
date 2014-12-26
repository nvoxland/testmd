package testmd.logic;

/**
 * Exception thrown by verification logic if the permutation cannot be verified.
 */
public class CannotVerifyException extends RuntimeException {
    public CannotVerifyException(String message) {
        super(message);
    }

    public CannotVerifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
