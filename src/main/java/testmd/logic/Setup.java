package testmd.logic;

/**
 * Interface to define setup logic in a {@link testmd.Permutation}.
 * Called only if previous test results are not defined OR previous results are not verified OR permutation results changed.
 * <br><br>
 * Extends runnable to make Spock-based testing cleaner by supporting a closure for implementation.
 *
 * This method should always throw a {@link testmd.logic.SetupResult}.
 * <ul>
 * <li>If {@link testmd.logic.SetupResult.Skip} is thrown, the permutation is skipped</li>
 * <li>If {@link testmd.logic.SetupResult.CannotVerify} is thrown, the permutation marked as "Cannot Verify" and the verification is not ran</li>
 * <li>If {@link testmd.logic.SetupResult#OK} is thrown, the permutation can be verified if need be</li>
 * </ul>
 * <b>IF NO EXCEPTION IS THROWN, THE TEST WILL THROW AN ERROR AND FAIL</b>
 */
public interface Setup extends Runnable {
    public void run() throws SetupResult;
}
