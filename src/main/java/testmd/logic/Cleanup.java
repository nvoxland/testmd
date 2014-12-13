package testmd.logic;

/**
 * Interface to define cleanup logic in a {@link testmd.Permutation}.
 * Called regardless of errors in Setup and Verification, so be sure to handle those cases.
 * <br><br>
 * Extends runnable to make Spock-based testing cleaner by supporting a closure for implementation.
 *
 * Any problems with cleanup should throw a {@link testmd.logic.CleanupException}.
 */
public interface Cleanup extends Runnable {
    public void run() throws CleanupException;
}
