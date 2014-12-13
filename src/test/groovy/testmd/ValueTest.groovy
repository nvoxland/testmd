package testmd

import spock.lang.Specification
import spock.lang.Unroll

class ValueTest extends Specification {

    def static otherFormat = new ValueFormat() {
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
        "a string" | ValueFormat.DEFAULT | "a string"
        "a string" | otherFormat          | "NOT a string"
        131        | ValueFormat.DEFAULT | "131"
        Collection | ValueFormat.DEFAULT | "java.util.Collection"
        null       | ValueFormat.DEFAULT | null
        null       | null                 | null
        null       | otherFormat         | "NOT null"
        "a string" | null                 | "a string"
    }

}
