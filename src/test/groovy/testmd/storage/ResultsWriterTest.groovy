package testmd.storage

import spock.lang.Specification
import testmd.PermutationResult
import testmd.storage.ResultsWriter
import testmd.util.StringUtils

class ResultsWriterTest extends Specification {
    def "output empty test"() {
        when:
        def out = new StringWriter()
        new ResultsWriter().write("com.example.Test", "my test name", [], out)

        then:
        out.toString() == StringUtils.read(this.class.classLoader.getResourceAsStream("testmd/example_output/empty.md"))

        cleanup:
        out.close()
    }

    def "output complex test"() {
        when:
        def permutations = createComplexPermutations()
        def out = new StringWriter()
        new ResultsWriter().write("com.example.Test", "complex test", permutations, out)

        then:
        out.toString() == StringUtils.read(this.class.classLoader.getResourceAsStream("testmd/example_output/complex.md"))

        cleanup:
        out && out.close()
    }

    def "output complex test with table"() {
        when:
        def permutations = createComplexPermutationsWithTable()
        def out = new StringWriter()
        new ResultsWriter().write("com.example.Test", "complex test with tables", permutations, out)

        then:
        out.toString() == StringUtils.read(this.class.classLoader.getResourceAsStream("testmd/example_output/complex_tables.md"))

        cleanup:
        out && out.close()
    }

    def createComplexPermutations() {
        def results = new ArrayList<PermutationResult>()

        results.add(new PermutationResult.Verified()
                .setParameters([
                "String Parameter 1" : "param 1",
                "String Parameter 2" : "param 2",
                "Int Parameter"      : "4",
                "Group Param"        : "a",
                "Integer Parameter"  : "42",
                "Class Parameter"    : "java.lang.Integer.class",
                "Multiline Parameter": "I have a line\nAnd another line\nAnd a third line\nThis  one  has  double  spaces"])
                .setNotes(["String note": "note goes here", "Int Note": "838"])
                .setResults(["String data": "I see data here", "int data": "3838"])
        )

        results.add(new PermutationResult.Unverified("Was too lazy")
                .setParameters([
                "String Parameter 1": "param 1 on permutation 2",
                "String Parameter 2": "param 2 on permutation 2",
                "Group Param"       : "b"])
                .setResults(["String data": "No notes, just one data"])
        )

        results.add(new PermutationResult.Invalid("Invalid: Something was wrong with the parameters")
                .setParameters([
                "String Parameter 1": "param 1 on permutation 3",
                "String Parameter 2": "param 2 on permutation 3",
                "Group Param"       : "a"])
                .setResults(["String data": "No notes, just one data"])
        )

        results.add(new PermutationResult.Verified()
                .setParameters([
                "String Parameter 1": "Just param 1 on permutation 3",
                "Group Param"       : "a"])
                .setResults(["String data": "No notes, just one data"]))

        results.add(new PermutationResult.Verified()
                .setParameters([
                "String Parameter 1" : "Short Parameter",
                "Group Param"        : "b",
                "Multiline Parameter": "A Longer param with\nthis on a second line"])
                .setResults(["String data": "No notes, just one data"])
        )

        return results
    }

    def createComplexPermutationsWithTable() {
        def results = new ArrayList<PermutationResult>()

        def tableColumns = ["table param 1", "table param 2", "table param 3", "table param x1"] as Set

        results.add(new PermutationResult.Verified().setParameters([
                "Param 1"      : "param 1 is a",
                "Param 2"      : "param 2 is a",
                "Group Param"  : "a",
                "table param 1": "tp1 is a",
                "table param 2": "tp2 is a"])
                .setTableParameters(tableColumns)
                .setResults(["out data": "Permutation 1"]))

        results.add(new PermutationResult.Unverified(null).setParameters([
                "Param 1"      : "param 1 is b",
                "Group Param"  : "b",
                "Param 2"      : "param 2 is b",
                "table param 2": "tp2 is b"])
                .setTableParameters(tableColumns)
                .setResults(["out data": "Permutation 2"]))

        results.add(new PermutationResult.Verified().setParameters([
                "Param 1"      : "param 1 is c",
                "Param 2"      : "param 2 is c",
                "table param 1": "tp1 is c",
                "table param 2": "tp2 is c",
                "table param 3": "tp3 is c",
                "Group Param"  : "b"])
                .setTableParameters(tableColumns)
                .setResults(["out data": "Permutation 3"])
                .setNotes(["more info": "Some notes for permutation 3"]))

        results.add(new PermutationResult.Unverified("Too lazy to test").setParameters([
                "Param 1"      : "param 1 is b",
                "Param 2"      : "param 2 is b",
                "table param 1": "tp1 is d",
                "table param 2": "tp2 is d",
                "table param 3": "tp3 is d",
                "Group Param"  : "b"])
                .setTableParameters(tableColumns)
                .setResults(["out data": "Permutation 4", "more out data": "Permutation 4 extra data"])
                .setNotes(["more info": "Some notes for this", "yet more info": "Even more notes for this"]))

        results.add(new PermutationResult.Unverified(null).setParameters([
                "Param 1"      : "param 1 is b",
                "Param 2"      : "param 2 is b",
                "Group Param"  : "b",
                "table param 2": "tp2 is e"])
                .setTableParameters(tableColumns)
                .setResults(["out data": "Permutation 5"]))

        results.add(new PermutationResult.Unverified("Too dark").setParameters([
                "Param 1"       : "param 1 is d",
                "Param 2"       : "param 2 is d",
                "Group Param"   : "b",
                "table param x1": "tpx1 is a"])
                .setTableParameters(tableColumns)
                .setResults(["out data": "Permutation 6"]))

        results.add(new PermutationResult.Unverified(null).setParameters([
                "Param 1"       : "param 1 is d",
                "Param 2"       : "param 2 is d",
                "Group Param"   : "b",
                "table param x1": "tpx1 is b"])
                .setTableParameters(tableColumns)
                .setResults(["out data": "Permutation 7\nWith a second line with | chars\nAnd another with | chars"]))

        results.add(new PermutationResult.Verified().setParameters([
                "String Parameter 1" : "Short Parameter",
                "Group Param"        : "a",
                "Multiline Parameter": "A Longer param with\nthis on a second line",
                "table param 2"      : "tp2 is b"])
                .setTableParameters(tableColumns)
                .setResults(["String data": "No notes, just one data"]))

        return results

    }
}
