package testmd

import spock.lang.Specification

class TestMDTest extends Specification {

    def "multiple calls to test return the same testManager if it is the same testGroup"() {
        when:
        def group1_1 = TestMD.test("group 1", "test name", TestMDTest).testManager
        def group2_1 = TestMD.test("group 2", "test name", TestMDTest).testManager
        def group2_2 = TestMD.test("group 2", "test name", TestMDTest).testManager
        def group1_2 = TestMD.test("group 1", "test name", TestMDTest).testManager

        then:
        group1_1.is(group1_2)
        group2_1.is(group2_2)

        !group1_1.is(group2_1)
    }

}
