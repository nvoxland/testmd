package testmd.logic;

/**
 * Base class for all results of {@link testmd.logic.Setup#run()}.
 * Actual exceptions thrown must be subclasses {@link testmd.logic.SetupResult.Skip} or {@link testmd.logic.SetupResult.CannotVerify} or the {@link #OK} static value.
 * <br><br>
 * Needed to be implemented as an exception to fit into the {@link java.lang.Runnable} interface for easier Spock testing.
 */
public abstract class SetupResult extends RuntimeException{

    public SetupResult(String message) {
        super(message);
    }

    /**
     * Result that specifies that the setup ran successfully.
     */
    public static OkResult OK = new OkResult();

    /**
     * Returns whether the permutation is a valid permutation that can be stored.
     */
    public abstract boolean isValid();

    /**
     * Returns wither thi permutation can be verified.
     */
    public abstract boolean canVerify();

    /**
     * Exception thrown when the permutation should be skipped because it is invalid for some reason.
     * No further testing will be done and no results will be stored.
     */
    public static class Skip extends SetupResult {

        public Skip(String message) {
            super(message);
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public boolean canVerify() {
            return false;
        }
    }

    /**
     * Exception thrown when the permutation cannot be verified. Results are stored as "unverified" and the verification method is not ran.
     */
    public static class CannotVerify extends SetupResult {

        public CannotVerify(String message) {
            super(message);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean canVerify() {
            return false;
        }

    }

    protected static class OkResult extends SetupResult {

        public OkResult() {
            super("Setup Successful");
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean canVerify() {
            return true;
        }
    }

}
