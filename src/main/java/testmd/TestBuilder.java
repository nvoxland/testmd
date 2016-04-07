package testmd;

import testmd.storage.TestManager;

import java.util.Map;

public class TestBuilder {
    private String testGroup;
    private String testName;
    private TestManager testManager;

    public TestBuilder(String testGroup, String testName, TestManager testManager) {
        this.testGroup = testGroup;
        this.testName = testName;
        this.testManager = testManager;
    }

    public String getTestName() {
        return testName;
    }

    /**
     * Creates and configures a new permutation with no parameters.
     */
    public Permutation withPermutation() throws Exception {
        return withPermutation((Map<String, Object>) null);
    }

    /**
     * Creates and configures a new permutation which is populated with the map values as parameters.
     */
    public Permutation withPermutation(Map<String, Object> parameters) throws Exception {
        return withPermutation(new Permutation(testGroup, testName, parameters));
    }

    /**
     * Registers a custom subclassed {@link testmd.Permutation} with TestMD.
     */
    public Permutation withPermutation(Permutation permutation) throws Exception {
        testManager.addPermutation(testName, permutation);

        return permutation;
    }

}
