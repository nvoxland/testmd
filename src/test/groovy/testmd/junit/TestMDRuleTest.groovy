package testmd.junit

import com.example.ExampleJUnitTest
import org.junit.runner.Description
import spock.lang.Specification
import testmd.TestMD

class TestMDRuleTest extends Specification {

    def "test name and class are correctly set"() {
        when:
        TestMD.setResultsManager(null)

        def rule = new TestMDRule()
        rule.starting(Description.createTestDescription(ExampleJUnitTest, "Display Name"))
        def permutation = rule.permutation()

        then:
        permutation.testRun.getTestClass() == "com.example.ExampleJUnitTest"
        permutation.testRun.getTestName() == "Display Name"

    }
}
