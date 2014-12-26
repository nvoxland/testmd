package testmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testmd.logic.*;
import testmd.util.StringUtils;

import java.util.*;

/**
 * Defines a test permutation to execute.
 * New permutation objects created through {@link TestMD#permutation()} or {@link TestMD#permutation(java.util.Map)} ) to ensure that they are property managed for saving results.
 * <br><br>
 * The lifecycle of a permutation is to call:
 * <ol>
 * <li>A previous run (if any) is looked up by comparing the values stored in {@link #addParameter(String, Object)} with previous runs</li>
 * <li>If a previous run is found and it was "verified", the values from {@link #addResult(String, Object)} are compared with the previous run and if the results are the same this test is assumed to be correct still and finished.</li>
 * <li>The setup implementation defined by {@link #setup(Runnable)}.</li>
 * <li>If the Setup object pass, the verification test defined by {@link #run(Runnable)} is executed</li>
 * <li>If run throws exceptions, the results are not saved and the test fails. If all permutations pass the results are saved for future tests.</li>
 * <li>Regardless of Setup and Verification, the Runnable object defined by {@link #cleanup(Runnable)} is executed</li>
 * </ol>
 * <br><br>
 * Format and additional information in the saved results can be managed with {@link #addNote(String, Object)} and {@link #asTable(java.util.Collection)}
 */
public class Permutation {

    private Map<String, Value> parameters = new HashMap<String, Value>();
    private Set<String> tableParameters = new HashSet<String>();
    private Map<String, Value> results = new HashMap<String, Value>();
    private Map<String, Value> notes = new HashMap<String, Value>();

    private Runnable setup;
    private Runnable cleanup;

    private TestMD testManager;

    protected Permutation(Map<String, Object> parameters) {
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                if (entry.getValue() != null) {
                    addParameter(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    protected void setTestManager(TestMD testManager) {
        this.testManager = testManager;
    }

    /**
     * Returns the parameters that uniquely identify this permutation.
     */
    public Map<String, Value> getParameters() {
        return parameters;
    }

    /**
     * Convenience method for {@link #addParameter(String, Object, ValueFormat)} using {@link testmd.ValueFormat.DefaultFormat}
     */
    public Permutation addParameter(String key, Object value) {
        addParameter(key, value, ValueFormat.DEFAULT);
        return this;
    }

    /**
     * Adds another parameter to uniquely identify this permutation.
     */
    public Permutation addParameter(String key, Object value, ValueFormat valueFormat) {
        parameters.put(key, new Value(value, valueFormat));
        return this;
    }

    /**
     * Returns the parameters which will be formatted as a table in storage.
     */
    public Set<String> getTableParameters() {
        return tableParameters;
    }


    /**
     * When formatting results for storage, store the given parameters as a table instead of separately.
     */
    public Permutation asTable(String... tableParameters) {
        if (tableParameters != null) {
            asTable(Arrays.asList(tableParameters));
        }

        return this;
    }

    /**
     * When formatting results for storage, store the given parameters as a table instead of separately.
     */
    public Permutation asTable(Collection<String> tableParameters) {
        if (tableParameters != null) {
            this.tableParameters.addAll(tableParameters);
        }
        return this;
    }

    /**
     * Returns notes defined for this permutation. Notes are not used in the execution of the test, they are simply values stored to the results file for future reference.
     */
    public Map<String, Value> getNotes() {
        return notes;
    }

    /**
     * Convenience method for {@link #addNote(String, Object, ValueFormat)}  using {@link ValueFormat.DefaultFormat}
     */
    public Permutation addNote(String key, Object value) {
        addNote(key, value, ValueFormat.DEFAULT);
        return this;
    }

    /**
     * Adds a note to this permutation.
     */
    public Permutation addNote(String key, Object value, ValueFormat valueFormat) {
        notes.put(key, new Value(value, valueFormat));
        return this;
    }

    /**
     * Returns the "results" of operation you want to verify.
     * The operation to generate results should be unit-test fast and is used to determine if the determine if the logic under test has changed how it interacts with the rest of the system.
     */
    public Map<String, Value> getResults() {
        return results;
    }

    /**
     * Convenience method for {@link #addResult(String, Object, ValueFormat)} using {@link ValueFormat.DefaultFormat}
     */
    public Permutation addResult(String key, Object value) {
        addResult(key, value, ValueFormat.DEFAULT);
        return this;
    }

    /**
     * Adds another result of the logic under test to compare with previous results and determine if retest is necessary.
     */
    public Permutation addResult(String key, Object value, ValueFormat valueFormat) {
        results.put(key, new Value(value, valueFormat));
        return this;
    }

    /**
     * Defines the setup logic to use for this permutation.
     * Runnable is passed for cleaner Spock tests, but an exception must be thrown describing if the setup was successful.
     * Throwing an exception for a result is strange, but needed due to Groovy syntax.
     * <ul>
     * <li>If {@link testmd.logic.SetupResult.Skip} is thrown, the permutation is skipped</li>
     * <li>If {@link testmd.logic.SetupResult.CannotVerify} is thrown, the permutation marked as "Cannot Verify" and the verification is not ran</li>
     * <li>If {@link testmd.logic.SetupResult#OK} is thrown, the permutation can be verified if need be</li>
     * </ul>
     * <b>IF NO EXCEPTION IS THROWN, THE TEST WILL THROW AN ERROR AND FAIL</b>
     */
    public Permutation setup(Runnable setup) {
        this.setup = setup;
        return this;
    }

    /**
     * Alternative to {@link #setup(Runnable)} if you would rather return a {@link testmd.logic.SetupResult} from your logic instead of throwing it.
     * A SetupResult object must still be returned, if null is returned the test will throw an error.
     */
    public Permutation setup(final Setup setup) {
        return this.setup(new Runnable() {
            @Override
            public void run() {
                SetupResult result = setup.run();
                if (result == null) {
                    throw new SetupResult.CannotVerify("No SetupResult returned from testmd.logic.Setup implementation "+setup.getClass().getName());
                }
                throw result;
            }
        });
    }

    /**
     * Defines the cleanup logic to use for this permutation.
     * Called regardless of errors in Setup and Verification, so be sure to handle those cases.
     * <br><br>
     * Any problems with cleanup should throw a {@link testmd.logic.CleanupException}
     */
    public Permutation cleanup(Runnable cleanup) {
        this.cleanup = cleanup;
        return this;
    }

    /**
     * Runs this permutation test. This method returns null because it should be called after all setup, cleanup, addParameter, etc. methods.
     * <br><br>
     * Logic is called only if (previous test results are not defined OR previous results are not verified OR permutation results changed) AND setup returned SetupResult.OK.
     * <br><br>
     * Within your verification logic:
     * <ul>
     * <li>If all tests pass successfully, no exceptions should be thrown</li>
     * <li>Any assertions in the logic will throw an AssertionException which will be reported correctly as a failed test</li>
     * <li>If it turns out that you cannot verify this permutation, throw {@link testmd.logic.CannotVerifyException}</li>
     * <li>Any other exceptions thrown will be reported as a test exception</li>
     * </ul>
     *
     */
    public void run(Runnable verification) throws Exception {
        assert testManager != null : "No TestManager set";

        PermutationResult previousResult = testManager.getPreviousResult(this);
        PermutationResult result = run(verification, previousResult);
        testManager.addNewResult(result);
    }

    /**
     * Returns the "key" used to uniquely identify this permutation. The key is used to lookup previous results and to manually search & find particular permutations.
     */
    public String getKey() {
        if (parameters.size() == 0) {
            return "";
        } else {
            return StringUtils.computeHash(StringUtils.join(parameters, ",", StringUtils.STANDARD_STRING_FORMAT, true));
        }
    }

    @Override
    public String toString() {
        return "Test Permutation [" + StringUtils.join(getParameters(), ", ", StringUtils.STANDARD_STRING_FORMAT, false) + "]";
    }

    /**
     * Returns the given map as a human readable string.
     */
    protected String toString(Map<String, Value> map) {
        List<String> out = new ArrayList<String>();
        for (Map.Entry<String, Value> entry : map.entrySet()) {
            out.add(entry.getKey() + "=\"" + entry.getValue().serialize() + "\"");
        }

        return StringUtils.join(out, ", ", false);
    }

    /**
     * The actual test logic called by {@link #run(Runnable)} after previous run has been found.
     */
    protected PermutationResult run(Runnable verification, PermutationResult previousRun) throws Exception, AssertionError {
        if (verification == null) {
            throw new RuntimeException("No verification logic set");
        }

        if (parameters.size() == 0) {
            throw new RuntimeException("No verification logic set");
        }

        Logger log = LoggerFactory.getLogger(Permutation.class);
        log.debug("----- Running Test Permutation" + this.toString() + " -----");

        if (previousRun != null) {
            if (previousRun.isVerified()) {
                log.debug("Previous test permutation run was verified");
                boolean allEqual = true;
                if (previousRun.getResults().size() == this.getResults().size()) {
                    for (Map.Entry<String, String> previousData : previousRun.getResults().entrySet()) {
                        Value thisRunValue = this.getResults().get(previousData.getKey());
                        String previousValue = previousData.getValue();

                        if (thisRunValue == null || !thisRunValue.serialize().equals(previousValue)) {
                            allEqual = false;
                            break;
                        }
                    }
                } else {
                    allEqual = false;
                }
                if (allEqual) {
                    log.debug("This test permutation is unchanged since the verified permutation. Do not run again");

                    return new PermutationResult.Verified(this);
                }
            } else {
                log.debug("Previous test permutation run was NOT verified");
            }
        }

        try {
            log.debug("Test permutation is being (re)tested");
            if (setup != null) {
                log.debug("Executing test permutation setup");

                SetupResult result = null;
                try {
                    setup.run();
                } catch (SetupResult setupResultThrown) {
                    result = setupResultThrown;
                }

                assert result != null : "No result returned (thrown) by setup";

                if (!result.isValid()) {
                    log.debug("Test permutation setup is not valid");
                    return new PermutationResult.Invalid(result.getMessage(), this);
                } else if (!result.canVerify()) {
                    log.debug("Cannot verify: " + result.getMessage());
                    return new PermutationResult.Unverified(result.getMessage(), this);
                }
            }
        } catch (Throwable e) {
            String message = "Error executing setup\n" +
                    "Description: " + toString(parameters) + "\n" +
                    "Notes: " + toString(notes) + "\n" +
                    "Results: " + toString(results);

            try {
                if (cleanup != null) {
                    cleanup.run();
                }
            } catch (Exception cleanupError) {
                log.error("Error executing cleanup after setup failure", cleanupError);
            }

            throw new RuntimeException(message, e);
        }

        Exception cleanupError = null;
        try {
            try {
                verification.run();
            } catch (CannotVerifyException e) {
                return new PermutationResult.Unverified(e.getMessage(), this);
            } catch (Throwable e) {
                if (e instanceof AssertionError) {
                    throw (AssertionError) e;
                }
                String message = "Error executing verification\n" +
                        "Description: " + toString(parameters) + "\n" +
                        "Notes: " + toString(notes) + "\n" +
                        "Results: " + toString(results);
                throw new RuntimeException(message, e);
            }
        } finally {
            if (cleanup != null) {
                try {
                    cleanup.run();
                } catch (Exception e) {
                    cleanupError = e;
                }
            }
        }

        if (cleanupError != null) {
            throw new RuntimeException("Error executing cleanup", cleanupError);
        }

        return new PermutationResult.Verified(this);
    }


}
