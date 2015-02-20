package testmd.storage

import spock.lang.Specification
import spock.lang.Unroll
import testmd.OldTestRun
import testmd.PermutationResult
import testmd.storage.ResultsReader
import testmd.storage.ResultsWriter
import testmd.util.StringUtils

import static org.junit.Assert.fail

class ResultsReaderTest extends Specification {

    def "read empty test"() {
        when:
        def reader = new InputStreamReader(openStream("empty.md"))
        def results = new ResultsReader().read("com.example.Test", reader)

        then:
        assert results.size() == 1
        assert results[0].getTestName() == "my test name"
        assert results[0].getTestClass() == "com.example.Test"
        assert results[0].getResults().size() == 0

        cleanup:
        reader && reader.close()
    }

    def openStream(fileName) {
        fileName = "testmd/example_output/${fileName}"
        def stream = this.class.classLoader.getResourceAsStream(fileName)
        if (stream == null) {
            fail("Could not open ${fileName}")
        }

        return stream;
    }

    @Unroll("#featureName: #resultsFile")
    def "saved results can be read"() {
        when:
        def reader = new InputStreamReader(openStream(resultsFile))
        def results = new ResultsReader().read("com.example.Test", reader)

        then:
        assertResultsSame(resultsFile, results, "com.example.Test", testName)

        cleanup:
        reader && reader.close()

        where:
        resultsFile | testName
        "empty.md" | ["my test name"]
        "complex.md" | ["complex test"]
        "complex_tables.md" | ["complex test with tables"]
        "multiple_tests.md" | ["multiple test", "multiple test: part 2"]
        "multiple_tests_tables.md" | ["complex test with tables", "complex test with tables: part 2", "can snapshot all tables in catalog", "can snapshot all tables in schema"]
    }

    def assertResultsSame(String expectedFile, List<OldTestRun> actualResults, String testClass, List<String> testName) {
        def writer = new ResultsWriter()
        def actualString = new StringWriter()

        def i = 0;
        for (def testRun : actualResults) {
            writer.write(testClass, testName[i++], testRun.getResults(), actualString)
        }

        def expectedStream = openStream(expectedFile)

        try {
            assert actualString.toString() == StringUtils.read(expectedStream)
        } finally {
            expectedStream.close()
            actualString.close()
        }

        true
    }
}
