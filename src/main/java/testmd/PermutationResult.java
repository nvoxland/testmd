package testmd;

import testmd.util.StringUtils;

import java.util.*;

/**
 * Contains the results of a just ran or a previously ran {@link testmd.Permutation}.
 * Parameters, results, notes, etc. are all stored sorted to avoid writing them in random/changing orders.
 * Actual instances created will be {@link testmd.PermutationResult.Verified} and {@link testmd.PermutationResult.Unverified} and {@link testmd.PermutationResult.Invalid}
 */
public abstract class PermutationResult implements Comparable<PermutationResult> {

    protected String notVerifiedMessage;
    private SortedMap<String, String> parameters = new TreeMap<String, String>();
    private SortedSet<String> tableParameters = new TreeSet<String>();
    private SortedMap<String, String> results = new TreeMap<String, String>();
    private SortedMap<String, String> notes = new TreeMap<String, String>();

    private String key = "";
    private String tableKey = "";

    /**
     * Creates an empty PermutationResult
     */
    public PermutationResult() {

    }

    /**
     * Creates a new PermutationResult with the same information as the given Permutation.
     */
    public PermutationResult(Permutation permutation) {
        for (Map.Entry<String, Value> entry : permutation.getParameters().entrySet()) {
            parameters.put(entry.getKey(), entry.getValue().serialize());
        }

        for (Map.Entry<String, Value> entry : permutation.getResults().entrySet()) {
            results.put(entry.getKey(), entry.getValue().serialize());
        }

        for (Map.Entry<String, Value> entry : permutation.getNotes().entrySet()) {
            notes.put(entry.getKey(), entry.getValue().serialize());
        }

        setTableParameters(permutation.getTableParameters());
        recomputeKey();
    }

    /**
     * Returns the permutation uniquely-identifying parameters
     */
    public SortedMap<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets parameters in this result. Overwrites any existing parameters.
     */
    public PermutationResult setParameters(Map<String, String> parameters) {
        this.parameters.clear();
        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (entry.getValue() != null) {
                    this.parameters.put(entry.getKey(), entry.getValue());
                }
            }
        }

        recomputeKey();

        return this;
    }

    /**
     * Returns the parameters to use as table columns when saving results.
     */
    public SortedSet<String> getTableParameters() {
        return tableParameters;
    }

    /**
     * Sets the parameters to use as table parameters in this result. Overwrites any existing settings.
     */
    public PermutationResult setTableParameters(Set<String> parameters) {
        this.tableParameters.clear();
        if (parameters != null) {
            for (String entry : parameters) {
                if (entry != null) {
                    this.tableParameters.add(entry);
                }
            }
        }

        recomputeKey();

        return this;
    }

    /**
     * Gets the notes associated with this permutation.
     */
    public SortedMap<String, String> getNotes() {
        return notes;
    }

    /**
     * Sets the notes associated with this permutation. Overwrites any existing settings.
     */
    public PermutationResult setNotes(Map<String, String> notes) {
        this.notes.clear();
        if (notes != null) {
            for (Map.Entry<String, String> entry : notes.entrySet()) {
                if (entry.getValue() != null) {
                    this.notes.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return this;
    }

    /**
     * Returns the "results" associated with this permutation
     */
    public SortedMap<String, String> getResults() {
        return results;
    }

    /**
     * Sets the "results" in this result. Overwrites any existing settings.
     */
    public PermutationResult setResults(Map<String, String> results) {
        this.results.clear();
        if (results != null) {
            for (Map.Entry<String, String> entry : results.entrySet()) {
                if (entry.getValue() != null) {
                    this.results.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return this;
    }

    /**
     * Returns true if this permutation result was correctly verified.
     */
    public abstract boolean isVerified();

    /**
     * Returns true if this permutation is a valid permutation.
     */
    public abstract boolean isValid();

    /**
     * Returns true if this permutation can be saved for future test comparisons.
     */
    public abstract boolean isSavable();

    /**
     * Returns the message given for why the permutation was not verified.
     * Returns true if result was verified or if no message was given.
     */
    public String getNotVerifiedMessage() {
        return notVerifiedMessage;
    }

    /**
     * Returns the key to uniquely and quickly identify the permutation. Must match {@link Permutation#getKey()}
     */
    public String getKey() {
        return key;
    }

    /**
     * Key used to uniquely identify each table when writing results to disk. Based on the values in the columns in {@link #getTableParameters()}
     */
    public String getTableKey() {
        return tableKey;
    }

    /**
     * Recomputes key and tableKey and stores them for faster performance.
     */
    protected void recomputeKey() {
        if (tableParameters != null && tableParameters.size() > 0) {
            Map<String, String> tableDescription = new TreeMap<String, String>();
            for (Map.Entry<String, String> rowEntry : parameters.entrySet()) {
                if (!tableParameters.contains(rowEntry.getKey())) {
                    tableDescription.put(rowEntry.getKey(), rowEntry.getValue());
                }

            }
            tableKey = toKey(tableDescription);
        } else {
            tableKey = "";
        }
        if (parameters.size() == 0) {
            key = "";
        } else {
            key = toKey(parameters);
        }
    }

    protected String toKey(Map<String, String> description) {
        return StringUtils.computeHash(StringUtils.join(description, ",", StringUtils.STANDARD_STRING_FORMAT, true));
    }


    @Override
    public int compareTo(PermutationResult o) {
        int i = this.getTableKey().compareTo(o.getTableKey());
        if (i == 0) {
            return this.getKey().compareTo(o.getKey());
        }
        return i;
    }

    /**
     * Base class for all Valid results.
     */
    protected abstract static class Valid extends PermutationResult {

        public Valid() {
        }

        public Valid(Permutation permutation) {
            super(permutation);
        }

        @Override
        public boolean isSavable() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;


        }
    }

    /**
     * Result for permutations that were successfully verified, either because the Verification logic passed or the results were unchanged since the last verification.
     */
    public static class Verified extends Valid {

        public Verified() {
            super();
        }

        public Verified(Permutation permutation) {
            super(permutation);
        }

        @Override
        public boolean isVerified() {
            return true;
        }

    }

    /**
     * Result for permutations that are valid but cannot be verified.
     */
    public static class Unverified extends Valid {
        public Unverified(String message) {
            this.notVerifiedMessage = message;
        }

        public Unverified(String message, Permutation permutation) {
            super(permutation);
            this.notVerifiedMessage = message;
        }

        @Override
        public boolean isVerified() {
            return false;
        }
    }

    /**
     * Results for permutations that are invalid and should be skipped.
     */
    public static class Invalid extends PermutationResult {

        public Invalid(String message) {
            this.notVerifiedMessage = message;
        }

        public Invalid(String message, Permutation permutation) {
            super(permutation);
            this.notVerifiedMessage = message;
        }

        @Override
        public boolean isVerified() {
            return false;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public boolean isSavable() {
            return true;
        }
    }

    /**
     * Result for permutations that failed validation.
     */
    public static class Failed extends PermutationResult {

        public Failed() {
        }

        public Failed(Permutation permutation) {
            super(permutation);
        }

        @Override
        public boolean isVerified() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isSavable() {
            return false;
        }
    }

}
