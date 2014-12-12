package testmd

import spock.lang.Specification
import spock.lang.Unroll

class ValueTest extends Specification {

    static otherFormat = new OutputFormat() {
        @Override
        String format(Object value) {
            return "NOT " + value;
        }
    }

    @Unroll
    def "getValue vs serialize vs toString"() {
        when:
        def value = new Value(baseValue, outputFormat)

        then:
        value.getValue() == baseValue
        value.serialize() == serialized
        value.toString() == serialized

        where:

        baseValue  | outputFormat         | serialized
        "a string" | OutputFormat.DEFAULT | "a string"
        "a string" | otherFormat          | "NOT a string"
        131        | OutputFormat.DEFAULT | "131"
        Collection | OutputFormat.DEFAULT | "java.util.Collection"
    }
}
