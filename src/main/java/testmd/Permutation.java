package testmd;

import org.junit.internal.AssumptionViolatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testmd.logic.*;
import testmd.storage.TestManager;
import testmd.util.LogUtil;
import testmd.util.StringUtils;

import java.util.*;

/**
 * Defines a test permutation to execute.
 * New permutation objects are created through {@link testmd.TestBuilder#withPermutation()} or {@link testmd.TestBuilder#withPermutation(java.util.Map)} ) to ensure that they are property managed for saving results.
 * <br><br>
 * The lifecycle of a permutation is to call:
 * <ol>
 * <li>A previous run (if any) is looked up by comparing the values stored in {@link #addParameter(String, Object)} with previous runs</li>
 * <li>If a previous run is found and it was "verified", the values from {@link #addOperation(String, Object)} are compared with the previous run and if the calls are the same this test is assumed to be correct still and finished.</li>
 * <li>The setup implementation defined by {@link #setup(Runnable)}.</li>
 * <li>If the Setup object pass, the verification test defined by {@link #run(Runnable)} is executed</li>
 * <li>If run throws exceptions, the results are not saved and the test fails. If all permutations pass the results are saved for future tests.</li>
 * <li>Regardless of Setup and Verification, the Runnable object defined by {@link #cleanup(Runnable)} is executed</li>
 * </ol>
 * <br><br>
 * Format and additional information in the saved results can be managed with {@link #addNote(String, Object)} and {@link #formattedAsTable(java.util.Collection)}
 */
public class Permutation {

    protected String testGroup;
    protected String testName;

    private String key;

    private Map<String, Value> parameters = new HashMap<String, Value>();
    private Set<String> tableParameters = new HashSet<String>();
    private Map<String, Value> operations = new HashMap<String, Value>();
    private Map<String, Value> notes = new HashMap<String, Value>();

    private Runnable setup;
    private Runnable cleanup;

    private TestManager testManager;

    private boolean forceRun = false;
    private PermutationResult testResult;
    private boolean wasRan = false;

    protected Permutation(String testGroup, String testName, Map<String, Object> parameters) {
        this.testGroup = testGroup;
        this.testName = testName;
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                if (entry.getValue() != null) {
                    addParameter(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public void setTestManager(TestManager testManager) {
        this.testManager = testManager;
    }

    public Permutation forceRun() {
        this.forceRun = true;
        return this;
    }

    /**
     * Returns the parameters that uniquely identify this permutation.
     */
    public Map<String, Value> getParameters() {
        return Collections.unmodifiableMap(parameters);
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
        if (value == null) {
            return this;
        }
        if (value instanceof Collection && ((Collection) value).size() == 0) {
            return this;
        }
        if (value instanceof Map && ((Map) value).size() == 0) {
            return this;
        }

        if (key.endsWith("_asTable")) {
            key = key.substring(0, key.length() - "_asTable".length());
            formattedAsTable(key);
        }

        parameters.put(key, new Value(value, valueFormat));
        this.key = null;
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
    public Permutation formattedAsTable(String... tableParameters) {
        if (tableParameters != null) {
            formattedAsTable(Arrays.asList(tableParameters));
        }

        return this;
    }

    /**
     * When formatting results for storage, store the given parameters as a table instead of separately.
     */
    public Permutation formattedAsTable(Collection<String> tableParameters) {
        if (tableParameters != null) {
            this.tableParameters.addAll(tableParameters);
        }
        return this;
    }

    public Permutation addParameters(Map<String, Object> parameters) {
        return addParameters(parameters, ValueFormat.DEFAULT);
    }

    public Permutation addParameters(Map<String, Object> parameters, ValueFormat valueFormat) {
        if (parameters != null) {
            for (Map.Entry<String, Object> param : parameters.entrySet()) {
                addParameter(param.getKey(), param.getValue(), valueFormat);
            }
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
     * Returns operation you want to verify.
     * The operation to generate operations should be unit-test fast and is used to determine if the determine if the logic under test has changed how it interacts with the rest of the system.
     */
    public Map<String, Value> getOperations() {
        return operations;
    }

    /**
     * Convenience method for {@link #addOperation(String, Object, ValueFormat)} using {@link ValueFormat.DefaultFormat}
     */
    public Permutation addOperation(String key, Object value) {
        addOperation(key, value, ValueFormat.DEFAULT);
        return this;
    }

    /**
     * Adds another operation of the logic under test to compare with previous operations and determine if retest is necessary.
     */
    public Permutation addOperation(String key, Object value, ValueFormat valueFormat) {
        operations.put(key, new Value(value, valueFormat));
        return this;
    }

    public Permutation addOperations(Map<String, Object> operations, ValueFormat valueFormat) {
        if (operations != null) {
            for (Map.Entry<String, Object> entry : operations.entrySet()) {
                addOperation(entry.getKey(), entry.getValue(), valueFormat);
            }
        }
        return this;
    }

    public Permutation addOperations(Map<String, Object> operations) {
        return addOperations(operations, ValueFormat.DEFAULT);
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
                    throw new SetupResult.CannotVerify("No SetupResult returned from testmd.logic.Setup implementation " + setup.getClass().getName());
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
     * Logic is called only if (previous test results are not defined OR previous results are not verified OR permutation operations changed) AND setup returned SetupResult.OK.
     * <br><br>
     * Within your verification logic:
     * <ul>
     * <li>If all tests pass successfully, no exceptions should be thrown</li>
     * <li>Any assertions in the logic will throw an AssertionException which will be reported correctly as a failed test</li>
     * <li>If it turns out that you cannot verify this permutation, throw {@link testmd.logic.CannotVerifyException}</li>
     * <li>Any other exceptions thrown will be reported as a test exception</li>
     * </ul>
     */
    public void run(Runnable verification) throws Exception {
        if (testManager == null) {
            throw new RuntimeException("No TestManager set");
        }

        Permutation duplicateKey = testManager.isDuplicateKey(testName, this);
        if (duplicateKey != null) {
            throw new RuntimeException("Key collision with another permutation. Make sure parameters fully differentiate all permutations.\nPermutation: " + this.toString() + "\nalso matches: " + duplicateKey.toString() + "\nwith operation " + StringUtils.join(duplicateKey.getOperations(), ",", false));
        }
        PermutationResult previousResult = testManager.getPreviousResult(testName, this);
        try {
            this.setTestResult(run(verification, previousResult));
        } catch (Throwable e) {
            String message = "Exception running permutation: " + e.getMessage() + "\n";
            if (e instanceof AssertionError) {
                message = "Failure running permutation: " + e.getMessage() + "\n";
            }
            LoggerFactory.getLogger(getClass()).error(message);

            setTestResult(new PermutationResult.Failed());
            throw e;
        }
    }

    /**
     * Returns the "key" used to uniquely identify this permutation. The key is used to lookup previous results and to manually search & find particular permutations.
     */
    public String getKey() {
        if (key == null) { //store computed key for performance reasons. Must clear out key attribute whenever parameters change
            key = StringUtils.computeKey(parameters);
        }
        return key;
    }

    @Override
    public String toString() {
        return "Test \"" + testName + "\", Permutation [" + StringUtils.join(getParameters(), ", ", StringUtils.STANDARD_STRING_FORMAT, false) + "]";
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
        log.debug("----- Running " + this.toString() + " -----");

        boolean forceRun = this.forceRun;

        String forceRunProperty = StringUtils.trimToNull(System.getProperty("testmd.forceRun"));
        if (forceRunProperty == null) {
            forceRunProperty = StringUtils.trimToNull(System.getProperty("testmd.forcerun"));
        }
        if (forceRunProperty != null && forceRunProperty.equalsIgnoreCase("true")) {
            LogUtil.warnOnce(log, "Forcing execution due to testmd.forcerun=true system property");
            forceRun = true;
        }
        if (testName.startsWith("!")) {
            forceRun = true;
            testName = testName.substring(1);
        }

        if (!forceRun && previousRun != null) {
            String currentHash = null;
            if (testManager != null) {
                currentHash = testManager.getCurrentTestHash(testGroup);
            }
            String savedHash = previousRun.getTestHash();

            if (currentHash != null && savedHash != null && !currentHash.equals(savedHash)) {
                LogUtil.warnOnce(log, "Forcing execution of all tests in " + testGroup + " due to version/hash change");
                forceRun = true;
            }
        }

        if (!forceRun && previousRun != null) {
            if (previousRun.isVerified()) {
                log.debug("Previous test permutation run was verified");
                boolean allEqual = true;
                if (previousRun.getResults().size() == this.getOperations().size()) {
                    for (Map.Entry<String, String> previousData : previousRun.getResults().entrySet()) {
                        Value thisRunValue = this.getOperations().get(previousData.getKey());
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
                } else {
                    log.debug("This test permutation changed since the verified permutation. Will test again");
                }
            } else {
                log.debug("Previous test permutation run was NOT verified");
            }
        } else if (forceRun) {
            LogUtil.warnOnce(log, "FORCE RUN TEST");
        }

        try {
            log.info("Test permutation is being (re)tested");
            wasRan = true;
            if (setup != null) {
                log.debug("Executing test permutation setup");

                SetupResult result = null;
                try {
                    setup.run();
                } catch (SetupResult setupResultThrown) {
                    result = setupResultThrown;
                }

                if (result == null) {
                    throw new RuntimeException("No result returned (thrown) by setup");
                }

                if (!result.isValid()) {
                    log.warn("Test permutation setup is not valid: " + result.getMessage() + "\n" + toLongString(4));
                    return new PermutationResult.Invalid(result.getMessage(), this);
                } else if (!result.canVerify()) {
                    log.debug("Cannot verify: " + result.getMessage() + "\n" + toLongString(4));
                    return new PermutationResult.Unverified(result.getMessage(), this);
                }
            }
        } catch (Throwable e) {
            String message = "Error executing setup\n" + toLongString(4);

            try {
                if (cleanup != null) {
                    cleanup.run();
                }
            } catch (Exception cleanupError) {
                log.error("Error executing cleanup after setup failure", cleanupError);
            }

            if (e instanceof AssumptionViolatedException) {
                throw e;
            } else {
                throw new SetupException(message, e);
            }
        }

        Exception cleanupError = null;
        try {
            try {
                verification.run();
            } catch (CannotVerifyException e) {
                return new PermutationResult.Unverified(e.getMessage(), this);
            } catch (Throwable e) {
                String message = (e instanceof AssertionError ? "Assertion Failed" : "Error") + " executing verification:\n" +
                        "Description: " + toString(parameters) + "\n" +
                        "Note(s): " + toString(notes) + "\n" +
                        "Operation(s): " + toString(operations);
                if (e instanceof AssertionError) {
                    throw new AssertionError(message + "\nCaused by: " + e.getMessage(), e);
                }
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

    protected String toLongString(int indent) {
        return StringUtils.indent(
                (parameters.size() > 0 ? "Description: " + toString(parameters) + "\n" : "") +
                        (notes.size() > 0 ? "Note(s): " + toString(notes) + "\n" : "") +
                        (operations.size() > 0 ? "Operation(s): " + toString(operations) : ""), indent);
    }


    public PermutationResult getTestResult() {
        return testResult;
    }

    protected void setTestResult(PermutationResult result) {
        this.testResult = result;
    }

    public boolean wasRan() {
        return wasRan;
    }

    public String formatNotVerifiedMessage(String message) {
        return message;
    }
}
