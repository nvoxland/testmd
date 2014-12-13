package testmd.logic

import spock.lang.Specification
import testmd.logic.SetupResult

class SetupResultTest extends Specification {

    def "verify/valid correct for subclasses"() {
        expect:
        type.isValid() == valid
        type.canVerify() == canVerify

        where:
        type                                         | valid | canVerify
        new SetupResult.OkResult()                   | true  | true
        new SetupResult.CannotVerify("test message") | true  | false
        new SetupResult.Skip("test message")      | false | false
    }
}
