package testmd.logic;

/**
 * Interface to define setup logic in a {@link testmd.Permutation}.
 * Called only if previous test results are not defined OR previous results are not verified OR permutation results changed.
 * <br><br>
 * This method should always return a {@link testmd.logic.SetupResult}.
 * <ul>
 * <li>If {@link testmd.logic.SetupResult.Skip} is thrown, the permutation is skipped</li>
 * <li>If {@link testmd.logic.SetupResult.CannotVerify} is thrown, the permutation marked as "Cannot Verify" and the verification is not ran</li>
 * <li>If {@link testmd.logic.SetupResult#OK} is thrown, the permutation can be verified if need be</li>
 * </ul>
 * <b>IF NULL IS RETURNED, THE TEST WILL THROW AN ERROR AND FAIL</b>
 */
public interface Setup {
    public SetupResult run() throws SetupResult;
}
