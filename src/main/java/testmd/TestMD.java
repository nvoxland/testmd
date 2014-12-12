package testmd;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMD {
    private static final Map<String, TestMD> instances = new HashMap<String, TestMD>();

    private final String testClass;
    private final String testName;
    private Map<String, PermutationResult> previousResults;
    private List<PermutationResult> newResults = new ArrayList<PermutationResult>();
    private List<Permutation> permutations = new ArrayList<Permutation>();

    public static TestMD define(Class testClass, final String testName) {
        return define(testClass.getName(), testName);
    }

    public static TestMD define(final String testClass, final String testName) {
        String key = testClass + ":" + testName;
        if (!instances.containsKey(key)) {
            final TestMD service = new TestMD(testClass, testName);
            instances.put(key, service);

            Runnable shutdownHook = new Runnable() {
                @Override
                public void run() {
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
                            new ResultsWriter().write(testClass, testName, service.newResults, fileWriter);
                        } finally {
                            fileWriter.flush();
                            fileWriter.close();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
        }
        return instances.get(key);
    }

    private TestMD(String testClass, String testName) {
        this.testClass = testClass;
        this.testName = testName;
    }

    public Permutation permutation() throws Exception {
        return permutation(null);
    }

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

    protected File getFile() {
        String testPackageDir = testClass.replaceFirst("\\.[^\\.]*$", "").replace(".", "/");
        String fileName = testClass.replaceFirst(".*\\.", "") + "." + escapeFileName(testName) + ".accepted.md";

        return new File(new File(getBaseDirectory(), testPackageDir), fileName);
    }

    private String escapeFileName(String name) {
        return name.replaceAll("\\s+", "_").replaceAll("[\\-\\.]", "");
    }

    protected File getBaseDirectory() {
        String testClassName = testClass.replace(".", "/") + ".class";

        URL resource = this.getClass().getClassLoader().getResource(testClassName);
        if (resource == null) {
            return new File(".").getAbsoluteFile();
        }
        File testClass = new File(resource.getFile());
        File classesRoot = new File(testClass.getAbsolutePath().replace(testClassName.replace("/", File.separator), ""));

        return new File(classesRoot.getParentFile().getParentFile(), "src/test/resources");
    }
}
