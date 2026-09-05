package eu.bcosp.vrlintellij.playground

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Exercises [VRLPlaygroundRunner] against the real `vector` binary when one is available on PATH,
 * skipping otherwise (e.g. on CI). This is what caught a real bug during development: `vector vrl`
 * (unlike the standalone `vrl` binary) always logs an "INFO vector::app: Log level is enabled."
 * line to stderr on startup, even on success - which without the VECTOR_LOG=off workaround in
 * [VRLPlaygroundRunner] made every successful run look like a failure.
 */
class VRLPlaygroundRunnerIntegrationTest {

    @Test
    fun `runs a real script against a sample event`() {
        assumeVectorAvailable()

        val result = VRLPlaygroundRunner.run(
            vectorBinaryPath = "vector",
            program = ".message = upcase!(.message)\n.status = \"processed\"",
            sampleEvent = """{"message": "hello world"}""",
        )

        assertTrue("expected Success but got $result", result is VRLPlaygroundResult.Success)
        result as VRLPlaygroundResult.Success
        assertEquals("\"processed\"", result.expressionResult)
        assertEquals("""{ "message": "HELLO WORLD", "status": "processed" }""", result.mutatedEvent)
    }

    @Test
    fun `a real compile error is reported, not masked as noise`() {
        assumeVectorAvailable()

        val result = VRLPlaygroundRunner.run(
            vectorBinaryPath = "vector",
            program = ".message = upcase(.message)",
            sampleEvent = "{}",
        )

        assertTrue("expected Failure but got $result", result is VRLPlaygroundResult.Failure)
        val message = (result as VRLPlaygroundResult.Failure).message
        assertTrue(message.contains("E103"))
        assertTrue(message.contains("unhandled fallible assignment"))
    }

    private fun assumeVectorAvailable() {
        val available = try {
            ProcessBuilder("vector", "--version").start().waitFor() == 0
        } catch (e: Exception) {
            false
        }
        assumeTrue("vector isn't on PATH - skipping", available)
    }
}
