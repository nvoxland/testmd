package testmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewTestRun extends TestRun {

    private List<Permutation> permutations = new ArrayList<>();
    private OldTestRun previousRun;

    public NewTestRun(String testClass, String testName, OldTestRun previousRun) {
        super(testClass, testName);
        this.previousRun = previousRun;
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
        return permutation(new Permutation(parameters));
    }

    /**
     * Registers a custom subclassed {@link testmd.Permutation} with TestMD.
     */
    public Permutation permutation(Permutation permutation) throws Exception {
        permutation.setTestRun(this);

        permutations.add(permutation);

        return permutation;
    }

    public List<Permutation> getPermutations() {
        return permutations;
    }

    public PermutationResult getPreviousResult(Permutation permutation) {
        if (previousRun == null) {
            return null;
        }
        for (PermutationResult result : previousRun.getResults()) {
            if (result.getKey().equals(permutation.getKey())) {
                return result;
            }
        }
        return null;
    }
}
