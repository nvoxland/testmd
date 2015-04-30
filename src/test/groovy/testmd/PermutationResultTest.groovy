package testmd

import spock.lang.Specification
import spock.lang.Unroll

class PermutationResultTest extends Specification {

    def "constructor with permutation"() {
        when:
        def format = new ValueFormat() {
            @Override
            String format(Object value) {
                return "REP"
            }
        }

        def permutation = new Permutation("Test Name", ["b": 1, "a": 2])
        permutation.addOperation("dataX", "x")
        permutation.addOperation("data5", 5)
        permutation.addOperation("dataCustom", "replace", format)
        permutation.addNote("noteC", "c")
        permutation.addNote("note8", 8)
        permutation.addNote("noteCustom", "replace", format)
        permutation.formattedAsTable(["b"])

        def result = new PermutationResult.Verified(permutation)

        then:
        result.getResults().toString() == "[data5:5, dataCustom:REP, dataX:x]"
        result.getParameters().toString() == "[a:2, b:1]"
        result.getNotes().toString() == "[note8:8, noteC:c, noteCustom:REP]"
        result.getTableParameters().toString() == "[b]"
    }

    def "getKey with no definition is empty string"() {
        expect:
        new PermutationResult.Verified().setParameters(new HashMap<String, String>()).getKey() == ""
    }


    @Unroll
    def "setParameters"() {
        when:
        def result = new PermutationResult.Verified()
        result.setParameters(["start": "should disappear"])
        result.setParameters(input)

        then:
        result.getParameters() == output

        where:
        input                | output
        ["a": "1", "b": "2"] | ["a": "1", "b": "2"]
        ["b": "2", "a": "1"] | ["a": "1", "b": "2"]
        new HashMap()        | new HashMap()
        null                 | new HashMap()
    }

    @Unroll
    def "setOutput"() {
        when:
        def result = new PermutationResult.Verified()
        result.setResults(["start": "should disappear"])
        result.setResults(input)

        then:
        result.getResults() == output

        where:
        input                | output
        ["a": "1", "b": "2"] | ["a": "1", "b": "2"]
        ["b": "2", "a": "1"] | ["a": "1", "b": "2"]
        new HashMap()        | new HashMap()
        null                 | new HashMap()
    }

    @Unroll
    def "setNotes"() {
        when:
        def result = new PermutationResult.Verified()
        result.setNotes(["start": "should disappear"])
        result.setNotes(input)

        then:
        result.getNotes() == output

        where:
        input                | output
        ["a": "1", "b": "2"] | ["a": "1", "b": "2"]
        ["b": "2", "a": "1"] | ["a": "1", "b": "2"]
        new HashMap()        | new HashMap()
        null                 | new HashMap()
    }

    @Unroll
    def "valid/verified/save logic is correct"() {
        expect:
        type.isValid() == valid
        type.isVerified() == verified
        type.isSavable() == savable

        where:
        type                                   | valid | verified | savable
        new PermutationResult.Verified()       | true  | true     | true
        new PermutationResult.Unverified(null) | true  | false    | true
        new PermutationResult.Failed()         | true  | false    | false
        new PermutationResult.Invalid(null)    | false | false    | true

    }
}
