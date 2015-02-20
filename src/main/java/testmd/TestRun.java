package testmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TestRun {

    private final String testClass;
    private final String testName;

    private List<PermutationResult> results = new ArrayList<>();

    public TestRun(String testClass, String testName) {
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
        this.results.add(result);
    }

    public List<PermutationResult> getResults() {
        return Collections.unmodifiableList(results);
    }
}
