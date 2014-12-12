package testmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testmd.util.StringUtils;

import java.util.*;

public class Permutation {

    private Map<String, Value> parameters = new HashMap<String, Value>();
    private Set<String> tableParameters;
    private Map<String, Value> results = new HashMap<String, Value>();
    private Map<String, Value> notes = new HashMap<String, Value>();

    private Setup setup;
    private Cleanup cleanup;

    private TestMD testManager;
    private String key = "";

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

    public Set<String> getTableParameters() {
        return tableParameters;
    }

    public Permutation setup(Setup setup) {
        this.setup = setup;
        return this;
    }

    public Map<String, Value> getParameters() {
        return parameters;
    }

    public Map<String, Value> getNotes() {
        return notes;
    }

    public Map<String, Value> getResults() {
        return results;
    }

    public Permutation addParameter(String key, Object value) {
        addParameter(key, value, OutputFormat.DEFAULT);
        return this;
    }

    public Permutation addParameter(String key, Object value, OutputFormat outputFormat) {
        parameters.put(key, new Value(value, outputFormat));
        recomputeKey();
        return this;
    }

    public Permutation addNote(String key, Object value) {
        addNote(key, value, OutputFormat.DEFAULT);
        return this;
    }

    public Permutation addNote(String key, Object value, OutputFormat outputFormat) {
        notes.put(key, new Value(value, outputFormat));
        return this;
    }

    public Permutation addResult(String key, Object value) {
        addResult(key, value, OutputFormat.DEFAULT);
        return this;
    }

    public Permutation addResult(String key, Object value, OutputFormat outputFormat) {
        results.put(key, new Value(value, outputFormat));
        return this;
    }

    public Permutation cleanup(Cleanup cleanup) {
        this.cleanup = cleanup;
        return this;
    }

    protected void recomputeKey() {
        if (parameters.size() == 0) {
            key = "";
        } else {
            key = toKey(parameters);
        }
    }

    public String getKey() {
        return key;
    }

    protected String toKey(Map<String, Value> description) {
        return StringUtils.computeHash(StringUtils.join(new TreeMap<String, Value>(description), ",", StringUtils.STANDARD_STRING_FORMAT, false));
    }

    @Override
    public String toString() {
        return "Test Permutation [" + StringUtils.join(getParameters(), ", ", StringUtils.STANDARD_STRING_FORMAT, false) + "]";
    }

    private String toString(Map<String, Value> map) {
        List<String> out = new ArrayList<String>();
        for (Map.Entry<String, Value> entry : map.entrySet()) {
            out.add(entry.getKey() + "=\"" + entry.getValue().serialize() + "\"");
        }

        return StringUtils.join(out, ", ", false);
    }

    public Permutation asTable(String... tableParameters) {
        if (tableParameters != null) {
            asTable(Arrays.asList(tableParameters));
        }

        return this;
    }

    public Permutation asTable(Collection<String> tableParameters) {
        this.tableParameters = new TreeSet<String>(tableParameters);
        return this;
    }

    public void run(Verification verification) throws Exception {
        if (testManager == null) {
            throw new RuntimeException("No TestManager set");
        }

        PermutationResult previousResult = testManager.getPreviousResult(this);
        PermutationResult result = run(verification, previousResult);
        testManager.addNewResult(result);
    }

    protected PermutationResult run(Verification verification, PermutationResult previousRun) throws Exception, AssertionError {
        if (verification == null) {
            throw new RuntimeException("No verification logic set");
        }

        Logger log = LoggerFactory.getLogger(Permutation.class);
        log.debug("----- Running Test Permutation" + this.toString() + " -----");
//        if (notRanMessage != null) {
//            log.debug("Not running test permutation: " + notRanMessage);
//            return new PermutationResult.Unverified(notRanMessage, this);
//        }

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

                SetupResult result = setup.run();

                if (result == null) {
                    throw new RuntimeException("No result returned by setup");
                } else {
                    if (!result.isValid()) {
                        log.debug("Test permutation setup is not valid");
                        return new PermutationResult.Invalid(result.getMessage(), this);
                    } else if (!result.canVerify()) {
                        log.debug("Cannot verify: " + result.getMessage());
                        return new PermutationResult.Unverified(result.getMessage(), this);
                    }
                }
            }
        } catch (Throwable e) {
            String message = "Error executing setup\n" +
                    "Description: " + toString(parameters) + "\n" +
                    "Notes: " + toString(notes) + "\n" +
                    "Results: " + toString(results);
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

    public static interface Setup {
        public SetupResult run() throws Exception;
    }

    public static interface Verification {
        public void run();
    }

    public static interface Cleanup {
        public void run();
    }

    public static class CannotVerifyException extends RuntimeException {
        public CannotVerifyException(String message) {
            super(message);
        }

        public CannotVerifyException(String message, Throwable cause) {
            super(message, cause);
        }
    }


}
