package testmd;

import testmd.storage.ResultsReader;
import testmd.storage.ResultsWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entry point class to create a new TestMD test. Primary method to define the test is {@link #test(String, String)}. When running in JUnit, consider using {@link testmd.junit.TestMDRule}
 * <br><br>
 * See <a href="https://github.com/nvoxland/testmd/blob/master/src/test/java/com/example/ExampleJUnitTest.java">ExampleJUnitTest</a>
 * and <a href="https://github.com/nvoxland/testmd/blob/master/src/test/groovy/com/example/ExampleSpockTest.groovy">ExampleSpockTest</a> for example usage.
 * <br><br>
 * The job of this class is to manage the lifecycle of tests and permutations. Permutations are created through this object so that they can be correctly saved to disk when the tests complete.
 * This class will ensure that files are only read and written once and so it follows a singleton pattern.
 */
public class TestMD {
    private static final Map<String, TestMD> instances = new HashMap<String, TestMD>();
    private static ResultsManager resultsManager = new ResultsManager();

    private final String testClass;
    private final String testName;
    private Map<String, PermutationResult> previousResults;
    private List<PermutationResult> newResults = new ArrayList<PermutationResult>();
    private List<Permutation> permutations = new ArrayList<Permutation>();
    private static String baseDirectory;

    static {
        baseDirectory = "../../src/test/resources";
        String baseDirectoryProperty = System.getProperty("testmd.base_directory");
        if (baseDirectoryProperty != null) {
            baseDirectory = baseDirectoryProperty;
        }
    }

    /**
     * Configures the directory relative to the classpath root where the class under test is stored to use as the base directory to store results.
     * Default value is "../../src/test/resources" which matches the Maven standard directory structure.
     * This value can also be set with the "testmd.base_directory" system property.
     */
    public static void setBaseDirectory(String baseDirectory) {
        TestMD.baseDirectory = baseDirectory;
    }

    /**
     * Convenience method for {@link #test(String, String)} for defining a test with a Class.
     */
    public static TestMD test(Class testClass, String testName) {
        return test(testClass.getName(), testName);
    }

    /**
     * Looks up or creates the TestMD manager for the given testClass and testName combination.
     */
    public static TestMD test(String testClass, String testName) {
        String key = testClass + ":" + testName;
        if (!instances.containsKey(key)) {
            TestMD service = new TestMD(testClass, testName);
            instances.put(key, service);

            if (resultsManager != null) {
                resultsManager.scheduleWriteResults(service);
            }
        }
        return instances.get(key);
    }

    private TestMD(String testClass, String testName) {
        this.testClass = testClass;
        this.testName = testName;
    }

    /**
     * Override the default {@link testmd.TestMD.ResultsManager} if you need custom logic.
     * Make sure this method is called before any {@link #test(String, String)} calls.
     */
    public static void setResultsManager(ResultsManager resultManager) {
        TestMD.resultsManager = resultManager;
    }

    /**
     * Creates and configures a new permutation with no parameters.
     */
    public Permutation permutation() throws Exception {
        return permutation(null);
    }

    /**
     * Creates and configures a new permutation which is populated with the map values as parameters.
     */
    public Permutation permutation(Map<String, Object> parameters) throws Exception {
        Permutation permutation = new Permutation(parameters);
        permutation.setTestManager(this);

        permutations.add(permutation);

        return permutation;
    }

    protected PermutationResult getPreviousResult(Permutation permutation) throws Exception {
        if (previousResults == null) {
            previousResults = new HashMap<String, PermutationResult>();

            File file = getFile();
            if (file.exists()) {
                FileReader reader = new FileReader(file);
                for (PermutationResult previousResult : new ResultsReader().read(reader)) {
                    previousResults.put(previousResult.getKey(), previousResult);
                }
            }
        }
        return previousResults.get(permutation.getKey());
    }

    protected void addNewResult(PermutationResult result) {
        newResults.add(result);
    }

    protected String getTestClass() {
        return testClass;
    }

    protected String getTestName() {
        return testName;
    }

    protected File getFile() {
        String testPackageDir = getTestClass().replaceFirst("\\.[^\\.]*$", "").replace(".", "/");
        String fileName = getTestClass().replaceFirst(".*\\.", "") + "." + escapeFileName(getTestName()) + ".accepted.md";

        return new File(new File(getBaseDirectory(), testPackageDir), fileName);
    }

    protected String escapeFileName(String name) {
        return name.replaceAll("\\s+", "_").replaceAll("[\\-\\.]", "");
    }

    protected File getBaseDirectory() {
        String testClassName = testClass.replace(".", "/") + ".class";

        URL resource = this.getClass().getClassLoader().getResource(testClassName);
        if (resource == null) {
            return new File(".").getAbsoluteFile();
        }

        int packageLevels = testClass.replaceAll("[^\\.]", "").length();

        File classRoot = new File(resource.getFile()).getParentFile();
        for (int i=0; i<packageLevels; i++) {
            classRoot = classRoot.getParentFile();
        }

        return new File(classRoot, baseDirectory);
    }

    /**
     * ResultsManager contains the logic used by TestMD to save results.
     * The default implementation uses a Runtime shutdown hook to persist results to disk.
     */
    public static class ResultsManager {

        protected void scheduleWriteResults(final TestMD service) {
            Runnable shutdownHook = new Runnable() {
                @Override
                public void run() {
                    writeResults(service);
                }
            };
            Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
        }

        protected void writeResults(TestMD service) {
            if (service.permutations.size() == 0) {
                return;
            }

            for (PermutationResult result : service.newResults) {
                if (!result.isSavable()) {
                    return;
                }
            }
            File file = service.getFile();
            file.getParentFile().mkdirs();

            try {
                FileWriter fileWriter = new FileWriter(file);
                try {
                    new ResultsWriter().write(service.getTestClass(), service.getTestName(), service.newResults, fileWriter);
                } finally {
                    fileWriter.flush();
                    fileWriter.close();
                }
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

    }
}
