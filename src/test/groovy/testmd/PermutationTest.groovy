package testmd

import spock.lang.Specification
import spock.lang.Unroll
import testmd.logic.CannotVerifyException
import testmd.logic.Cleanup
import testmd.logic.Setup
import testmd.logic.SetupResult
import testmd.logic.Verification


class PermutationTest extends Specification {

    def setupRunCount
    def executeRunCount
    def cleanupRunCount
    Permutation permutation

    def setup() {
        setupRunCount = 0
        executeRunCount = 0
        cleanupRunCount = 0

        permutation = new Permutation([a: 1, b: 2])
                .addResult("out", 100)
                .setup({setupRunCount++; throw SetupResult.OK} as Setup)
                .cleanup({ cleanupRunCount++ } as Cleanup)
    }

    def "constructor with null parameters"() {
        expect:
        new Permutation(null).getParameters().size() == 0
    }

    def "run with no previous result"() {
        when:
        def result = permutation.run({ executeRunCount++ } as Verification, null)

        then:
        setupRunCount == 1
        executeRunCount == 1
        cleanupRunCount == 1
        assert result.isValid()
        assert result.isVerified()
        assert result.isSavable()

    }

    def "run when previous previous run was verified"() {
        when:
        def previousRun = new PermutationResult.Verified(permutation)

        def result = permutation.run({ executeRunCount++ } as Verification, previousRun)

        then:
        setupRunCount == 0
        executeRunCount == 0
        cleanupRunCount == 0

        assert result.isValid()
        assert result.isVerified()
        assert result.isSavable()
    }

    def "run when previous previous run was not verified"() {
        when:
        def previousRun = new PermutationResult.Unverified("Test message", permutation)
        def result = permutation.run({ executeRunCount++ } as Verification, previousRun)

        then:
        setupRunCount == 1
        executeRunCount == 1
        cleanupRunCount == 1

        assert result.isValid()
        assert result.isVerified()
        assert result.isSavable()
    }

    @Unroll
    def "run when previous previous run was verified but different output"() {
        when:
        def previousRun = new PermutationResult.Verified()
        previousRun.setParameters(["a": "1", "b": "2"])
        previousRun.setResults(output)
        def result = permutation.run({ executeRunCount++ } as Verification, previousRun)

        then:
        setupRunCount == 1
        executeRunCount == 1
        cleanupRunCount == 1

        assert result.isValid()
        assert result.isVerified()
        assert result.isSavable()

        where:
        output << [ ["out": "555"], ["not-out": "12"], ["out": "100", "not-out": "12"],  new HashMap<String, String>(), null ]

    }

    def "run when setup throws exception"() {
        when:
        permutation.setup({throw new RuntimeException("Testing exception")} as Setup)
        permutation.run({ executeRunCount++ } as Verification, null)

        then:
        def e = thrown(RuntimeException)
        assert e.message.startsWith("Error executing setup")
        assert e.cause.message == "Testing exception"
    }


    def "run when verification throws exception"() {
        when:
        permutation.run({ throw new RuntimeException("verification problem") } as Verification, null)

        then:
        def e = thrown(RuntimeException)
        assert e.message.startsWith("Error executing verification")
        assert e.cause.message == "verification problem"
    }

    def "run when setup returns CannotVerify"() {
        when:
        permutation.setup({setupRunCount++; throw new SetupResult.CannotVerify("cannot verify message")} as Setup)
        def result = permutation.run({ executeRunCount++ } as Verification, null)

        then:
        setupRunCount == 1
        executeRunCount == 0
        cleanupRunCount == 0
        assert !result.isVerified()
        assert result.isValid()
        result.getNotVerifiedMessage() == "cannot verify message"
        assert result.isSavable()
    }

    def "run when setup returns Invalid"() {
        when:
        permutation.setup({setupRunCount++; throw new SetupResult.Skip("invalid message")} as Setup)
        def result = permutation.run({ executeRunCount++ } as Verification, null)

        then:
        setupRunCount == 1
        executeRunCount == 0
        cleanupRunCount == 0
        assert !result.isVerified()
        assert !result.isValid()
        result.getNotVerifiedMessage() == "invalid message"
        assert result.isSavable()
    }

    def "run when setup returns null"() {
        when:
        permutation.setup({setupRunCount++;} as Setup)
        permutation.run({ executeRunCount++ } as Verification, null)

        then:
        def e = thrown(RuntimeException)
        assert e.message.startsWith("Error executing setup")
        assert e.cause.message == "No result returned (thrown) by setup"

        setupRunCount == 1
        executeRunCount == 0
    }

    def "run when cleanup throws an error"() {
        when:
        permutation.cleanup({throw new RuntimeException("cleanup error")} as Cleanup)
        permutation.run({ executeRunCount++ } as Verification, null)

        then:
        def e = thrown(RuntimeException)
        assert e.message.startsWith("Error executing cleanup")
        assert e.cause.message == "cleanup error"

        setupRunCount == 1
        executeRunCount == 1
    }

    def "run with null verification logic"() {
        when:
        permutation.run(null, null)

        then:
        def e = thrown(RuntimeException)
        assert e.message.startsWith("No verification logic set")

        setupRunCount == 0
        executeRunCount == 0
    }

    def "run when verification logic throws CannotVerifyException"() {
        when:
        def result = permutation.run({ executeRunCount++; throw new CannotVerifyException("testing not verify") } as Verification, null)

        then:
        setupRunCount == 1
        executeRunCount == 1
        cleanupRunCount == 1
        assert !result.isVerified()
        assert result.isValid()
        result.getNotVerifiedMessage() == "testing not verify"
        assert result.isSavable()
    }
}
