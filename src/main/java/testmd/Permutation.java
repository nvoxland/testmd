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
 * <li>The {@link testmd.logic.Setup} implementation defined by {@link #setup(testmd.logic.Setup)}.</li>
 * <li>If the Setup object pass, the {@link testmd.logic.Verification} test defined by {@link #run(testmd.logic.Verification)} is executed</li>
 * <li>If run throws exceptions, the results are not saved and the test fails. If all permutations pass the results are saved for future tests.</li>
 * <li>Regardless of Setup and Verification, the {@link testmd.logic.Cleanup} object defined by {@link #cleanup(testmd.logic.Cleanup)} is executed</li>
 * </ol>
 * <br><br>
 * Format and additional information in the saved results can be managed with {@link #addNote(String, Object)} and {@link #asTable(java.util.Collection)}
 */
public class Permutation {

    private Map<String, Value> parameters = new HashMap<String, Value>();
    private Set<String> tableParameters = new HashSet<String>();
    private Map<String, Value> results = new HashMap<String, Value>();
    private Map<String, Value> notes = new HashMap<String, Value>();

    private Setup setup;
    private Cleanup cleanup;

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
     * Defines the {@link testmd.logic.Setup} logic to use for this permutation
     */
    public Permutation setup(Setup setup) {
        this.setup = setup;
        return this;
    }

    /**
     * Defines the {@link testmd.logic.Cleanup} logic to use for this permutation
     */
    public Permutation cleanup(Cleanup cleanup) {
        this.cleanup = cleanup;
        return this;
    }

    /**
     * Runs this permutation test. This method returns null because it should be called after all setup, cleanup, addParameter, etc. methods.
     */
    public void run(Verification verification) throws Exception {
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
     * The actual test logic called by {@link #run(testmd.logic.Verification)} after previous run has been found.
     */
    protected PermutationResult run(Verification verification, PermutationResult previousRun) throws Exception, AssertionError {
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
