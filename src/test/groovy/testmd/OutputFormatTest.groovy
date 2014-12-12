package testmd

import spock.lang.Specification
import spock.lang.Unroll

class OutputFormatTest extends Specification {

    static otherFormat = new OutputFormat() {
        @Override
        String format(Object value) {
            return "NOT " + value;
        }
    }

    @Unroll
    def "OutputFormat.DEFAULT format"() {
        expect:
        OutputFormat.DEFAULT.format(input) == output

        where:
        input                                                      | output
        null                                                       | null
        "1"                                                        | "1"
        "longer string"                                            | "longer string"
        3L                                                         | "3"
        3I                                                         | "3"
        5.6F                                                       | "5.6"
        Integer                                                    | "java.lang.Integer"
        [] as Object[]                                             | ""
        ["a string", 3L, 5.6F, Collection.class, null] as Object[] | "a string, 3, 5.6, java.util.Collection, null"
        [] as List                                                 | ""
        ["a string", 3L, 5.6F, Collection.class, null] as List     | "a string, 3, 5.6, java.util.Collection, null"
        new Value(5, otherFormat)                                  | "NOT 5"
    }

    @Unroll
    def "CollectionFormat format"() {
        expect:
        new OutputFormat.CollectionFormat(itemFormat).format(input) == output

        where:
        input                                                  | itemFormat           | output
        null                                                   | OutputFormat.DEFAULT | null
        [] as List                                             | OutputFormat.DEFAULT | ""
        ["a string", 3L, 5.6F, Collection.class, null] as List | OutputFormat.DEFAULT | "a string, 3, 5.6, java.util.Collection, null"
        ["21", "65"]                                           | otherFormat          | "NOT 21, NOT 65"
    }

    @Unroll
    def "ArrayFormat format"() {
        expect:
        new OutputFormat.ArrayFormat(itemFormat).format(input) == output

        where:
        input                                                      | itemFormat           | output
        null                                                       | OutputFormat.DEFAULT | null
        [] as Object[]                                             | OutputFormat.DEFAULT | ""
        ["a string", 3L, 5.6F, Collection.class, null] as Object[] | OutputFormat.DEFAULT | "a string, 3, 5.6, java.util.Collection, null"
        ["21", "65"] as Object[]                                   | otherFormat          | "NOT 21, NOT 65"

    }
}
