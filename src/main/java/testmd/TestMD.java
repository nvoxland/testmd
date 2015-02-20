package testmd;

import junit.framework.TestResult;
import org.slf4j.LoggerFactory;
import testmd.storage.ResultsReader;
import testmd.storage.ResultsWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

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
    private static final Map<File, Map<String, NewTestRun>> newTestRuns = new HashMap<>();
    private static final Map<File, Map<String, OldTestRun>> oldTestRuns = new HashMap<>();
    private static ResultsManager resultsManager = new ResultsManager();

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

    public static NewTestRun test(Class testClass, String testName) {
        return test(testClass.getName(), testName);
    }

    /**
     * Convenience method for {@link #test(String, String)} for defining a test with a Class.
     */
    public static NewTestRun test(Class testClass, String testName, Class isSameClassRoot) {
        return test(testClass.getName(), testName, isSameClassRoot);
    }

    public static NewTestRun test(String testClass, String testName) {
        try {
            return test(testClass, testName, Class.forName(testClass));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Looks up or creates the TestMD manager for the given testClass and testName combination.
     */
    public static NewTestRun test(String testClass, String testName, Class inSameClassRoot) {

        File file = new TestMD().getFile(testClass, testName, inSameClassRoot);
        String testKey = testClass + ":" + testName;

        try {
            if (!oldTestRuns.containsKey(file)) {
                HashMap<String, OldTestRun> map = new HashMap<>();
                oldTestRuns.put(file, map);
                if (file.exists()) {
                    LoggerFactory.getLogger(TestMD.class).debug("Found previous run stored at " + file.getAbsolutePath());

                    FileReader reader = new FileReader(file);
                    for (OldTestRun oldTestRun : new ResultsReader().read(testClass, reader)) {
                        map.put(testKey, oldTestRun);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading previous results", e);
        }

//        if (newTestRuns.containsKey(file)) {
//            throw new RuntimeException("Already defined a test "+testName+" in "+testClass);
//        }

        NewTestRun newTestRun = new NewTestRun(testClass, testName, oldTestRuns.get(file).get(testKey));

        Map<String, NewTestRun> newTestRunMap = newTestRuns.get(file);
        if (newTestRunMap == null) {
            newTestRunMap = new HashMap<>();
            newTestRuns.put(file, newTestRunMap);
        }
        newTestRunMap.put(testKey, newTestRun);

        if (resultsManager != null) {
            resultsManager.scheduleWriteResults(file);
        }


        return newTestRun;
    }

    protected File getFile(String testClass, String testName, Class inSameClassRoot) {
        String testPackageDir = testClass.replaceFirst("\\.[^\\.]*$", "").replace(".", "/");
        String fileName = testClass.replaceFirst(".*\\.", "") + ".accepted.md";

        return new File(new File(getBaseDirectory(inSameClassRoot), testPackageDir), fileName);
    }

    /**
     * Override the default {@link testmd.TestMD.ResultsManager} if you need custom logic.
     * Make sure this method is called before any {@link #test(String, String)} calls.
     */
    public static void setResultsManager(ResultsManager resultManager) {
        TestMD.resultsManager = resultManager;
    }

    protected File getBaseDirectory(Class inSameClassRoot) {
        String testClassName = inSameClassRoot.getName().replace(".", "/") + ".class";

        URL resource = this.getClass().getClassLoader().getResource(testClassName);
        if (resource == null) {
            return new File(".").getAbsoluteFile();
        }

        int packageLevels = testClassName.replaceAll("[^/]", "").length();

        File classRoot = new File(resource.getFile()).getParentFile();
        for (int i = 0; i < packageLevels; i++) {
            classRoot = classRoot.getParentFile();
        }

        return new File(classRoot, baseDirectory);
    }

    /**
     * ResultsManager contains the logic used by TestMD to save results.
     * The default implementation uses a Runtime shutdown hook to persist results to disk.
     */
    public static class ResultsManager {

        private Set<File> files = new HashSet<>();

        protected void scheduleWriteResults(final File file) {
            if (files.add(file)) {
                Runnable shutdownHook = new Runnable() {
                    @Override
                    public void run() {
                        writeResults(file);
                    }
                };
                Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
            }
        }

        protected void writeResults(File file) {
            Map<String, TestRun> finalResults = new HashMap<>();

            boolean canSave = true;
            for (NewTestRun newTestRun : newTestRuns.get(file).values()) {
                if (!canSave) {
                    break;
                }
                if (newTestRun.getPermutations().size() == 0) {
                    LoggerFactory.getLogger(getClass()).debug("No permutations to save for  " + file.getAbsolutePath());
                    continue;
                }

                for (PermutationResult result : newTestRun.getResults()) {
                    if (!result.isSavable()) {
                        LoggerFactory.getLogger(getClass()).debug("Cannot save results to " + file.getAbsolutePath());
                        canSave = false;
                        break;
                    }
                }

                if (canSave) {
                    finalResults.put(newTestRun.getTestName(), newTestRun);
                }
            }

            if (canSave) {
                for (OldTestRun oldTestRun : oldTestRuns.get(file).values()) {
                    if (!finalResults.containsKey(oldTestRun.getTestName())) {
                        finalResults.put(oldTestRun.getTestName(), oldTestRun);
                    }
                }

                SortedSet<TestRun> toSave = new TreeSet<>(new Comparator<TestRun>() {
                    @Override
                    public int compare(TestRun o1, TestRun o2) {
                        return o1.getTestName().compareTo(o2.getTestName());
                    }
                });
                toSave.addAll(finalResults.values());

                new ResultsWriter().write(file, toSave);
            }
        }

    }
}
