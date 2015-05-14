package testmd;

import testmd.storage.ResultsReader;
import testmd.storage.ResultsWriter;
import testmd.storage.TestManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

    public static TestBuilder test(Object specificationContext, Class inSameClassRoot) {
        try {
            String testName;
            if (!specificationContext.getClass().getName().equals("org.spockframework.runtime.SpecificationContext")) {
                throw new RuntimeException("Can only use the TestMD.test(Object, Class) method passing this.specificationContext in Spock tests. You passed a "+specificationContext.getClass().getName()+". Use other versions of Testmd.test() instead.");
            }

            Object currentIteration = specificationContext.getClass().getMethod("getCurrentIteration").invoke(specificationContext);

            Object parent = currentIteration.getClass().getMethod("getParent").invoke(currentIteration);

            if (parent == null) {
                testName = (String) currentIteration.getClass().getMethod("getName").invoke(currentIteration);
            } else {
                testName = (String) parent.getClass().getMethod("getName").invoke(parent);
            }

            Object specInfo = specificationContext.getClass().getMethod("getCurrentSpec").invoke(specificationContext);
            String packageName = (String) specInfo.getClass().getMethod("getPackage").invoke(specInfo);
            String fileName = ((String) specInfo.getClass().getMethod("getFilename").invoke(specInfo)).replaceFirst("\\..*", "");

            return test(packageName+"."+fileName, testName, inSameClassRoot);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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
    public static TestBuilder test(Class testClass, String testName, Class inSameClassRoot) {
        return test(testClass.getName(), testName, inSameClassRoot);
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
