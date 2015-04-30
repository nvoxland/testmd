package testmd.junit;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import testmd.Permutation;
import testmd.TestMD;

import java.util.Map;

public class TestMDRule extends TestWatcher {

    private String testClassName;
    private String testName;
    private Class<?> inSameRootAs;

    @Override
    protected void starting(Description description) {
        inSameRootAs = description.getTestClass();
        testClassName = inSameRootAs.getName();
        testName = description.getMethodName();

    }

    public Permutation permutation(Map<String, Object> parameters) throws Exception {
        return TestMD.test(testClassName, testName, inSameRootAs).withPermutation(parameters);
    }

    public Permutation permutation() throws Exception {
        return TestMD.test(testClassName, testName, inSameRootAs).withPermutation();
    }
}
