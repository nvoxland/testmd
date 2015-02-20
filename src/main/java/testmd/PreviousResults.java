package testmd;

import java.util.*;

public class PreviousResults {

    private final String testClass;
    private final String testName;

    private Map<String, PermutationResult> results = new HashMap<>();

    public PreviousResults(String testClass, String testName) {
        this.testClass = testClass;
        this.testName = testName;
    }

    public String getTestClass() {
        return testClass;
    }

    public String getTestName() {
        return testName;
    }

    public void addResult(PermutationResult result) {
        this.results.put(result.getKey(), result);
    }

    public List<PermutationResult> getResults() {
        return Collections.unmodifiableList(new ArrayList<>(results.values()));
    }

    public PermutationResult getResult(String key) {
        return results.get(key);
    }
}
