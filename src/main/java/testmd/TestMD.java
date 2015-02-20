package testmd;

import testmd.storage.ResultsReader;
import testmd.storage.ResultsWriter;
import testmd.storage.TestManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Entry point class to create a new TestMD test. Primary method to define the test is {@link #test(String, String, Class)}. When running in JUnit, consider using {@link testmd.junit.TestMDRule}
 * <br><br>
 * See <a href="https://github.com/nvoxland/testmd/blob/master/src/test/java/com/example/ExampleJUnitTest.java">ExampleJUnitTest</a>
 * and <a href="https://github.com/nvoxland/testmd/blob/master/src/test/groovy/com/example/ExampleSpockTest.groovy">ExampleSpockTest</a> for example usage.
 * <br><br>
 * The job of this class is to manage the lifecycle of tests and permutations. Permutations are created through this object so that they can be correctly saved to disk when the tests complete.
 * This class will ensure that files are only read and written once and so it follows a singleton pattern.
 */
public class TestMD {

    private static Map<String, TestManager> testManagers = new HashMap<>();
    private static ResultsReader resultsReader = new ResultsReader();
    private static ResultsWriter resultsWriter = new ResultsWriter();

    public TestMD() {
    }

    /**
     * Convenience method for {@link #test(String, String, Class)} for defining a test group based on a class name.
     * The testClass is used for the inSameClassRoot parameter.
     */
    public static TestBuilder test(Class testClass, String testName) {
        return test(testClass.getName(), testName, testClass);
    }

    /**
     * Convenience method for {@link #test(String, String, Class)} for defining a test group based on a class name.
     */
    public static TestBuilder test(Class testClass, String testName, Class isSameClassRoot) {
        return test(testClass.getName(), testName, isSameClassRoot);
    }

    /**
     * Creates a TestBuilder for the given testGroup and testName combination.
     * Results are stored in the same source path as the inSameClassRoot file.
     */
    public static TestBuilder test(String testGroup, String testName, Class inSameClassRoot) {
        TestManager testManager = testManagers.get(testGroup);
        if (testManager == null) {
            testManager = createTestManager(testGroup, inSameClassRoot);
            testManager.init();
            testManagers.put(testGroup, testManager);
        }
        return testManager.getBuilder(testName);
    }

    public static TestManager createTestManager(String testGroup, Class inSameClassRoot) {
        return new TestManager(testGroup, inSameClassRoot, resultsReader, resultsWriter);
    }

    public static void setResultsReader(ResultsReader resultsReader) {
        TestMD.resultsReader = resultsReader;
    }

    public static void setResultsWriter(ResultsWriter resultsWriter) {
        TestMD.resultsWriter = resultsWriter;
    }
}
