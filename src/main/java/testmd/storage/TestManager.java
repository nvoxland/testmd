package testmd.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testmd.*;
import testmd.util.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * ResultsManager contains the logic used by TestMD to save results.
 * The default implementation uses a Runtime shutdown hook to persist results to disk.
 */
public class TestManager {

    private final String testGroup;
    private final Class inSameClassRoot;
    private String baseOutputDirectory;

    private final Map<String, List<Permutation>> permutations = new HashMap<>();
    private final Map<String, PreviousResults> previousResults = new HashMap<>();
    private final Map<String, String> currentTestHashes = new HashMap<>();
    private ResultsReader resultsReader;
    private ResultsWriter resultsWriter;


    public TestManager(String testGroup, Class inSameClassRoot, ResultsReader resultsReader, ResultsWriter resultsWriter) {
        this.testGroup = testGroup;
        this.inSameClassRoot = inSameClassRoot;
        this.resultsReader = resultsReader;
        this.resultsWriter = resultsWriter;

        String baseDirectoryProperty = System.getProperty("testmd.base_directory");
        if (baseDirectoryProperty != null) {
            baseOutputDirectory = baseDirectoryProperty;
        } else {
            baseOutputDirectory = getDefaultBaseDirectory();
        }
    }

    public void init() {
        File file = this.getOutputFile();
        if (!this.currentTestHashes.containsKey(testGroup)) {
            this.currentTestHashes.put(testGroup, this.readTestHash());
        }

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

    protected String readTestHash() {
        String testHashes = "";
        Class testClass = null;
        try {
            testClass = Class.forName(testGroup);
        } catch (ClassNotFoundException e) {
            LoggerFactory.getLogger(getClass()).debug("Cannot find class for test " + testGroup + ". Cannot check source hash");
        }

        while (testClass != null) {
            if (includeInTestHash(testClass)) {
                String testHash = computeSourceHash(testClass);
                if (testHash == null) {
                    return null;
                }

                testHashes += testHash + "\n";
            }
            testClass = testClass.getSuperclass();
        }

        if (testHashes.length() == 0) {
            LoggerFactory.getLogger(getClass()).debug("No test class hashes found.");
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(testHashes.getBytes());
            byte[] digest = md.digest();
            return new String(StringUtils.encodeHex(digest)).substring(0, 6);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean includeInTestHash(Class clazz) {
        String name = clazz.getCanonicalName();
        if (name == null) {
            return false;
        }

        URL classUrl = this.getClass().getClassLoader().getResource(name.replace(".", "/") + ".class");
        if (classUrl == null || !classUrl.getProtocol().equals("file") || classUrl.toExternalForm().contains("jar:")) {
            return false;
        }

        return name.startsWith(testGroup.replaceFirst("\\..*", "."));
    }

    protected String computeSourceHash(Class clazz) {
        File sourceFile = getClassSource(clazz);
        if (sourceFile == null) {
            LoggerFactory.getLogger(getClass()).debug("Cannot find source file for "+clazz.getName()+". Cannot check source hash");
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            try (InputStream is = Files.newInputStream(sourceFile.toPath());
                 DigestInputStream dis = new DigestInputStream(is, md)) {
                byte[] buf = new byte[20480];
                while (dis.read(buf) != -1) {
                    ; //digest is updating
                }
            }
            byte[] digest = md.digest();
            return new String(StringUtils.encodeHex(digest)).substring(0, 6);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected File getClassSource(Class clazz) {
        String className = clazz.getCanonicalName();
        URL classUrl = this.getClass().getClassLoader().getResource(className.replace(".", "/") + ".class");
        if (classUrl == null) {
            return null;
        }
        int packageLevels = className.replaceAll("[^.]", "").length();

        File classRoot = new File(classUrl.getFile()).getParentFile();
        for (int i = 0; i < packageLevels; i++) {
            classRoot = classRoot.getParentFile();
        }

        List<File> sourceRoots = Arrays.asList(
                new File(classRoot, "../../src/test/groovy"),
                new File(classRoot, "../../src/test/java")
        );
        List<String> extensions = Arrays.asList(".groovy", ".java");

        for (File sourceRoot : sourceRoots) {
            for (String extension : extensions) {
                File file = new File(sourceRoot, className.replace(".", "/") + extension);
                if (file.exists()) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * Configures the directory relative to the classpath root where the class under test is stored to use as the base directory to store results.
     * Default value is "../../src/test/resources" which matches the Maven standard directory structure.
     * This value can also be set with the "testmd.base_directory" system property.
     */
    public void setBaseDirectory(String baseDirectory) {
        this.baseOutputDirectory = baseDirectory;
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
                if (result != null) {
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
            log.debug("No permutations executed for " + testGroup + ", do not write results");
            return;
        }

        if (canSave) {
            resultsWriter.write(getOutputFile(), currentTestHashes.get(testGroup), finalResults.values());
        }
    }

    public TestBuilder getBuilder(String testName) {
        return new TestBuilder(testGroup, testName, this);
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

    protected File getOutputFile() {
        String testPackageDir = testGroup.replaceFirst("\\.[^\\.]*$", "").replace(".", "/");
        String fileName = testGroup.replaceFirst(".*\\.", "") + ".accepted.md";

        return new File(new File(getOutputBase(inSameClassRoot), testPackageDir), fileName);
    }

    protected File getOutputBase(Class inSameClassRoot) {
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

        return new File(classRoot, baseOutputDirectory);
    }

    public String getCurrentTestHash(String testName) {
        return currentTestHashes.get(testName);
    }

    public PermutationResult getPreviousResult(String testName, Permutation permutation) {
        PreviousResults results = previousResults.get(testName);
        if (results == null) {
            return null;
        }
        return results.getResult(permutation.getKey());
    }

    public Permutation isDuplicateKey(String testName, Permutation permutation) {
        List<Permutation> permutations = this.permutations.get(testName);
        for (Permutation otherPermutation : permutations) {
            if (otherPermutation != permutation && otherPermutation.getKey().equals(permutation.getKey())) {
                return otherPermutation;
            }
        }
        return null;
    }
}
