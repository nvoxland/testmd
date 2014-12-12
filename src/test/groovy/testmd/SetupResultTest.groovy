package testmd

import spock.lang.Specification

class SetupResultTest extends Specification {

    def "verify/valid correct for subclasses"() {
        expect:
        type.isValid() == valid
        type.canVerify() == canVerify

        where:
        type                                         | valid | canVerify
        new SetupResult.OkResult()                   | true  | true
        new SetupResult.CannotVerify("test message") | true  | false
        new SetupResult.Invalid("test message")      | false | false
    }
}
