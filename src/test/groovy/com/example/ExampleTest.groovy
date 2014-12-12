package com.example

import spock.lang.Specification
import testmd.Permutation
import testmd.TestMD

class ExampleTest extends Specification {

    def "numbers are less than 10"() {
        expect:
        def test = TestMD.forTest(ExampleTest.class.name, "numbers are less than 10")

        test.permutation([number: number, weight: weight])
                .addResult("multiple", number * 63)
                .addResult("weighted", number * weight)
                .run({ number < 10 } as Permutation.Verification)

        where:
        number | weight
        1      | 0.3
        2      | 0.4
        3      | 0.5
        4      | 0.6
    }

    def "numbers are more than 10 as a table"() {
        expect:
        def test = TestMD.forTest(ExampleTest.class.name, "numbers are more than 10 as a table")

        test.permutation([number: number, weight: weight])
                .asTable(["number"])
                .addResult("weighted", number * weight)
                .run({ number > 10 } as Permutation.Verification)

        where:
        number | weight
        10     | 0.4
        20     | 0.3
        33     | 0.23
        21     | 0.3
        11     | 0.4
        22     | 0.3
    }
}
