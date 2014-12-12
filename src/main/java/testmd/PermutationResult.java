package testmd;

import testmd.util.StringUtils;

import java.util.*;

public abstract class PermutationResult implements Comparable<PermutationResult> {

    protected String notRanMessage;
    private SortedMap<String, String> parameters = new TreeMap<String, String>();
    private SortedSet<String> tableParameters = new TreeSet<String>();
    private SortedMap<String, String> results = new TreeMap<String, String>();
    private SortedMap<String, String> notes = new TreeMap<String, String>();

    private String key = "";
    private String tableKey = "";

    public PermutationResult() {

    }

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

    public SortedMap<String, String> getParameters() {
        return parameters;
    }

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

    public SortedSet<String> getTableParameters() {
        return tableParameters;
    }

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

    public SortedMap<String, String> getNotes() {
        return notes;
    }

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

    public SortedMap<String, String> getResults() {
        return results;
    }

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

    public abstract boolean isVerified();

    public abstract boolean isValid();

    public abstract boolean isSavable();

    public String getNotRanMessage() {
        return notRanMessage;
    }

    public String getKey() {
        return key;
    }

    public String getTableKey() {
        return tableKey;
    }

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
        return StringUtils.computeHash(StringUtils.join(description, ",", StringUtils.STANDARD_STRING_FORMAT, false));
    }


    @Override
    public int compareTo(PermutationResult o) {
        int i = this.getTableKey().compareTo(o.getTableKey());
        if (i == 0) {
            return this.getKey().compareTo(o.getKey());
        }
        return i;
    }

    private abstract static class Valid extends PermutationResult {

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

    public static class Unverified extends Valid {
        public Unverified(String message) {
            this.notRanMessage = message;
        }

        public Unverified(String message, Permutation permutation) {
            super(permutation);
            this.notRanMessage = message;
        }

        @Override
        public boolean isVerified() {
            return false;
        }
    }

    public static class Invalid extends PermutationResult {

        public Invalid(String message) {
            this.notRanMessage = message;
        }

        public Invalid(String message, Permutation permutation) {
            super(permutation);
            this.notRanMessage = message;
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
