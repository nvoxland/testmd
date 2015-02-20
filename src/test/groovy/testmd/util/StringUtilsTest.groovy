package testmd.util

import org.codehaus.groovy.util.StringUtil
import spock.lang.Specification
import spock.lang.Unroll

class StringUtilsTest extends Specification {

    static def mockFormatter = new StringUtils.JoinFormat() {
        @Override
        String toString(Object obj) {
            return "MOCK!";
        }
    }

    @Unroll
    def "join string collection array"() {
        expect:
        StringUtils.join(input, delimiter, sorted) == output

        where:
        input           | delimiter | sorted | output
        null            | null      | true   | null
        null            | null      | false  | null
        null            | null      | true   | null
        []              | ","       | false  | ""
        ["z", "x", "a"] | ","       | false  | "z,x,a"
        ["z", "x", "a"] | " : "     | false  | "z : x : a"
        ["z", "x", "a"] | null      | false  | "z,x,a"
        ["z", "x", "a"] | ","       | true   | "a,x,z"
    }

    @Unroll
    def "join object collection"() {
        expect:
        StringUtils.join(input, delimiter as String, format, sorted) == output

        where:
        input           | delimiter | format                     | sorted | output
        [] as ArrayList | ","       | StringUtils.STANDARD_STRING_FORMAT | false  | ""
        ["z", "x", "a"] | ","       | StringUtils.STANDARD_STRING_FORMAT | false  | "z,x,a"
        ["z", "x", "a"] | " : "     | StringUtils.STANDARD_STRING_FORMAT | false  | "z : x : a"
        ["z", "x", "a"] | null      | StringUtils.STANDARD_STRING_FORMAT | false  | "z,x,a"
        ["z", "x", "a"] | ","       | StringUtils.STANDARD_STRING_FORMAT | true   | "a,x,z"
        [13L, 5L, 8L]   | ","       | StringUtils.STANDARD_STRING_FORMAT | true   | "5,8,13"
        ["z", "x", "a"] | ","       | mockFormatter              | false  | "MOCK!,MOCK!,MOCK!"

    }


    @Unroll
    def "join with map"() {
        expect:
        StringUtils.join(input, delimiter, format, sorted) == value

        where:
        input                             | delimiter | format                     | sorted | value
        new HashMap()                     | ","       | StringUtils.STANDARD_STRING_FORMAT | false  | ""
        [key2: "a"]                       | ","       | StringUtils.STANDARD_STRING_FORMAT | false  | "key2=a"
        [key2: "a", key3: "b"]            | ","       | StringUtils.STANDARD_STRING_FORMAT | false  | "key2=a,key3=b"
        [key2: "a", key3: "b"]            | "X"       | StringUtils.STANDARD_STRING_FORMAT | false  | "key2=aXkey3=b"
        [key2: "a", key3: "b", key1: "c"] | ", "      | StringUtils.STANDARD_STRING_FORMAT | false  | "key2=a, key3=b, key1=c"
        [key2: "a", key3: "b", key1: "c"] | ", "      | StringUtils.STANDARD_STRING_FORMAT | true   | "key1=c, key2=a, key3=b"
        [key2: "a", key3: "b", key1: "c"] | null      | StringUtils.STANDARD_STRING_FORMAT | true   | "key1=c,key2=a,key3=b"
        [2L: "a", 3L: "b", 1L: "c"]       | ", "      | StringUtils.STANDARD_STRING_FORMAT | true   | "1=c, 2=a, 3=b"
    }

    @Unroll
    def "trimToEmpty"() {
        expect:
        StringUtils.trimToEmpty(input) == output

        where:
        input      | output
        null       | ""
        ""         | ""
        "xyz"      | "xyz"
        "   xyz"   | "xyz"
        "xyz  "    | "xyz"
        "   xyz  " | "xyz"
    }

    @Unroll
    def "trimToNull"() {
        expect:
        StringUtils.trimToNull(input) == output

        where:
        input      | output
        null       | null
        ""         | null
        "xyz"      | "xyz"
        "   xyz"   | "xyz"
        "xyz  "    | "xyz"
        "   xyz  " | "xyz"
    }

    @Unroll
    def "pad"() {
        expect:
        StringUtils.pad(input, pad) == output

        where:
        input   | pad | output
        null    | 0   | ""
        null    | 3   | "   "
        ""      | 0   | ""
        ""      | 3   | "   "
        " "     | 3   | "   "
        "abc"   | 2   | "abc"
        "abc"   | 3   | "abc"
        "abc  " | 3   | "abc"
        "abc"   | 5   | "abc  "
        "abc "  | 5   | "abc  "
    }

    @Unroll
    def "repeat"() {
        expect:
        StringUtils.repeat(input, times) == output

        where:
        input | times | output
        "x"   | 3     | "xxx"
        "x"   | 0     | ""
        null  | 3     | null
    }

    @Unroll
    def "indent"() {
        expect:
        StringUtils.indent(input, depth) == output

        where:
        input | depth | output
        "x"   | 3     | "   x"
        "x"   | 0     | "x"
        null  | 3     | null
    }

    @Unroll
    def "toStringFormat"() {
        expect:
        StringUtils.STANDARD_STRING_FORMAT.toString(obj) == expected

        where:
        obj                       | expected
        null                      | null
        ""                        | ""
        "x"                       | "x"
        ["a", "b"]                | "[a, b]"
        ["a", "b"] as String[]    | "[a, b]"
        [1, 2]                    | "[1, 2]"
        [1, 2] as String[]        | "[1, 2]"
        ["a", [2, 1], ["z":"a", "y":"b"]] | "[[1, 2], [y=b, z=a], a]"
    }

    @Unroll
    def "Default JoinFormat.compare method"() {
        expect:
        StringUtils.STANDARD_STRING_FORMAT.compare(o1, o2) == expected

        where:
        o1  | o2  | expected
        "a" | "b" | -1
        "b" | "a" | 1
        3   | 4   | -1
        5   | 3   | 1
        "1" | 2   | -1
        2   | "1" | 1
        2   | "11" | 1 //doing a string compare since types are not compatible
    }

    @Unroll
    def "computeHash"() {
        expect:
        StringUtils.computeHash(input) == output

        where:
        input | output
        null | "null"
        "x" | "11f6ad"
        "y" | "95cb0b"

    }

    def "read"() {
        expect:
        StringUtils.read(null) == null

        StringUtils.read(this.class.classLoader.getResourceAsStream("testmd/example_output/empty.md")).trim() == '''**NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY**

# Test: "my test name" #

**NO PERMUTATIONS**'''
    }
}
