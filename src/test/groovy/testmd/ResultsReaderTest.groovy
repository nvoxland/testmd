package testmd

import spock.lang.Specification
import spock.lang.Unroll
import testmd.util.StringUtils

import static org.junit.Assert.fail

class ResultsReaderTest extends Specification {

    def "read empty test"() {
        when:
        def reader = new InputStreamReader(openStream("empty.md"))
        def results = new ResultsReader().read(reader)

        then:
        assert results.size() == 0

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
        def results = new ResultsReader().read(reader)

        then:
        assertResultsSame(resultsFile, results, "com.example.Test", testName)

        cleanup:
        reader && reader.close()

        where:
        resultsFile | testName
        "empty.md" | "my test name"
        "complex.md" | "complex test"
        "complex_tables.md" | "complex test with tables"
    }

    def assertResultsSame(String expectedFile, List<PermutationResult> actualResults, String testClass, String testName) {
        def writer = new ResultsWriter()
        def actualString = new StringWriter()

        writer.write(testClass, testName, actualResults, actualString)

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
