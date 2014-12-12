package testmd.junit;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import testmd.Permutation;
import testmd.TestMD;

import java.util.Map;

public class TestMDRule extends TestWatcher {

    private String testClassName;
    private String testName;

    @Override
    protected void starting(Description description) {
        testClassName = description.getTestClass().getName();
        testName = description.getMethodName();

    }

    public Permutation permutation(Map<String, Object> parameters) throws Exception {
        return TestMD.define(testClassName, testName).permutation(parameters);
    }

    public Permutation permutation() throws Exception {
        return TestMD.define(testClassName, testName).permutation();
    }
}
