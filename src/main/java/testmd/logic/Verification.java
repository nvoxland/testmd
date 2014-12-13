package testmd.logic;

/**
 * Interface to define verification logic in a {@link testmd.Permutation}.
 * Called only if (previous test results are not defined OR previous results are not verified OR permutation results changed) AND setup returned SetupResult.OK.
 * <br><br>
 * <ul>
 * <li>If all tests pass successfully, no exceptions should be thrown</li>
 * <li>Any assertions in the logic will throw an AssertionException which will be reported correctly as a failed test</li>
 * <li>If it turns out that you cannot verify this permutation, throw {@link testmd.logic.CannotVerifyException}</li>
 * <li>Any other exceptions thrown will be reported as a test exception</li>
 * </ul>
 * Extends runnable to make Spock-based testing cleaner by supporting a closure for implementation.
 *
 */
public interface Verification extends Runnable {
    public void run() throws CannotVerifyException, AssertionError;
}
