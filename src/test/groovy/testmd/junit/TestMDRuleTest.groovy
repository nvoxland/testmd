package testmd.junit

import com.example.ExampleJUnitTest
import org.junit.runner.Description
import spock.lang.Specification
import testmd.TestMD
import testmd.storage.ResultsWriter

class TestMDRuleTest extends Specification {

    def setup() {
        TestMD.setResultsWriter(null)
    }

    def cleanup() {
        TestMD.setResultsWriter(new ResultsWriter())
    }

    def "test name and class are correctly set"() {
        when:
        def rule = new TestMDRule()
        rule.starting(Description.createTestDescription(ExampleJUnitTest, "Display Name"))
        def permutation = rule.permutation()

        then:
        permutation.testManager.testGroup == "com.example.ExampleJUnitTest"
        permutation.testName == "Display Name"

    }
}
