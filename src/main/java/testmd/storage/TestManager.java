package testmd.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testmd.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * ResultsManager contains the logic used by TestMD to save results.
 * The default implementation uses a Runtime shutdown hook to persist results to disk.
 */
public class TestManager {

    private final String testGroup;
    private final Class inSameClassRoot;
    private String baseDirectory;

    private final Map<String, List<Permutation>> permutations = new HashMap<>();
    private final Map<String, PreviousResults> previousResults = new HashMap<>();
    private ResultsReader resultsReader;
    private ResultsWriter resultsWriter;


    public TestManager(String testGroup, Class inSameClassRoot, ResultsReader resultsReader, ResultsWriter resultsWriter) {
        this.testGroup = testGroup;
        this.inSameClassRoot = inSameClassRoot;
        this.resultsReader = resultsReader;
        this.resultsWriter = resultsWriter;

        String baseDirectoryProperty = System.getProperty("testmd.base_directory");
        if (baseDirectoryProperty != null) {
            baseDirectory = baseDirectoryProperty;
        } else {
            baseDirectory = getDefaultBaseDirectory();
        }
    }

    public void init() {
        File file = this.getFile();

        try {
            if (file.exists()) {
                LoggerFactory.getLogger(TestMD.class).debug("Found previous run stored at " + file.getAbsolutePath());

                FileReader reader = new FileReader(file);
                for (PreviousResults readResults : resultsReader.read(testGroup, reader)) {
                    this.previousResults.put(readResults.getTestName(), readResults);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading previous results", e);
        }

        if (resultsWriter == null) {
            LoggerFactory.getLogger(getClass()).warn("Cannot save " + testGroup + ": No ResultsWriter defined");
        } else {
            this.scheduleWriteResults();
        }
    }

    protected String getDefaultBaseDirectory() {
        return "../../src/test/resources";
    }

    /**
     * Configures the directory relative to the classpath root where the class under test is stored to use as the base directory to store results.
     * Default value is "../../src/test/resources" which matches the Maven standard directory structure.
     * This value can also be set with the "testmd.base_directory" system property.
     */
    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    protected void scheduleWriteResults() {
            Runnable shutdownHook = new Runnable() {
                @Override
                public void run() {
                    writeResults();
                }
            };
            Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
    }

    protected void writeResults() {
        SortedMap<String, PreviousResults> finalResults = new TreeMap<>();

        Logger log = LoggerFactory.getLogger(getClass());
        if (permutations.size() == 0) {
            log.debug("No permutations to save for  " + testGroup);
            return;
        }

        boolean canSave = true;
        boolean somethingRan = false;
        for (Map.Entry<String, List<Permutation>> entry : permutations.entrySet()) {
            if (!canSave) {
                break;
            }
            String testName = entry.getKey();
            List<Permutation> permutationList = entry.getValue();

            for (Permutation permutation : permutationList) {
                if (!canSave) {
                    break;
                }
                if (permutation.wasRan()) {
                    somethingRan = true;
                }

                PermutationResult result = permutation.getTestResult();
                if (result.isSavable()) {
                    PreviousResults results = finalResults.get(testName);
                    if (results == null) {
                        results = new PreviousResults(testGroup, testName);
                        finalResults.put(testName, results);
                    }
                    results.addResult(result);
                } else {
                    log.debug("Not saving " + testGroup);
                    canSave = false;
                    break;
                }
            }
        }

        boolean onlyOneTestRan = finalResults.size() == 1;
        for (Map.Entry<String, PreviousResults> entry : this.previousResults.entrySet()) {
            String testName = entry.getKey();

            if (!finalResults.containsKey(testName)) {
                if (onlyOneTestRan && this.previousResults.size() > 1) {
                    //only one test ran this time but there used to be others. Probably running a single test manually
                    finalResults.put(testName, entry.getValue());
                } else {
                    log.info("Test " + testName + " was in the accepted results, but not in the test suite. Removing it from the accepted file.");
                    somethingRan = true;
                }
            }
        }


        if (!somethingRan) {
            log.debug("No permutations executed for "+testGroup+", do not write results");
            return;
        }

        if (canSave) {
            resultsWriter.write(getFile(), finalResults.values());
        }
    }

    public TestBuilder getBuilder(String testName) {
        return new TestBuilder(testName, this);
    }

    public void addPermutation(String testName, Permutation permutation) {
        List<Permutation> list = permutations.get(testName);
        if (list == null) {
            list = new ArrayList<>();
            permutations.put(testName, list);
        }

        list.add(permutation);
        permutation.setTestManager(this);
    }

    protected File getFile() {
        String testPackageDir = testGroup.replaceFirst("\\.[^\\.]*$", "").replace(".", "/");
        String fileName = testGroup.replaceFirst(".*\\.", "") + ".accepted.md";

        return new File(new File(getBaseDirectory(inSameClassRoot), testPackageDir), fileName);
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


    public PermutationResult getPreviousResult(String testName, Permutation permutation) {
        PreviousResults results = previousResults.get(testName);
        if (results == null) {
            return null;
        }
        return results.getResult(permutation.getKey());
    }
}
