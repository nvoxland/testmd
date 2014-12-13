package testmd.logic;

/**
 * Exception thrown by {@link Verification#run()} if the permutation cannot be verified.
 */
public class CannotVerifyException extends RuntimeException {
    public CannotVerifyException(String message) {
        super(message);
    }

    public CannotVerifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
