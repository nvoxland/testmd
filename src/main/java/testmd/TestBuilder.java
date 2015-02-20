package testmd;

import testmd.storage.TestManager;

import java.util.Map;

public class TestBuilder {
    private String testName;
    private TestManager testManager;

    public TestBuilder(String testName, TestManager testManager) {
        this.testName = testName;
        this.testManager = testManager;
    }

    public String getTestName() {
        return testName;
    }

    /**
     * Creates and configures a new permutation with no parameters.
     */
    public Permutation permutation() throws Exception {
        return permutation((Map<String, Object>) null);
    }

    /**
     * Creates and configures a new permutation which is populated with the map values as parameters.
     */
    public Permutation permutation(Map<String, Object> parameters) throws Exception {
        return permutation(new Permutation(testName, parameters));
    }

    /**
     * Registers a custom subclassed {@link testmd.Permutation} with TestMD.
     */
    public Permutation permutation(Permutation permutation) throws Exception {
        testManager.addPermutation(testName, permutation);

        return permutation;
    }

}
