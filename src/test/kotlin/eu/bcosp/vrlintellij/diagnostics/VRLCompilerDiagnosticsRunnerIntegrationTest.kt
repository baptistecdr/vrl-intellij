package eu.bcosp.vrlintellij.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Exercises [VRLCompilerDiagnosticsRunner] and [VRLCompilerDiagnosticsParser] together against the
 * real `vector` binary when one is available on PATH, skipping otherwise (e.g. on CI) - see
 * [eu.bcosp.vrlintellij.playground.VRLPlaygroundRunnerIntegrationTest] for the same pattern.
 */
class VRLCompilerDiagnosticsRunnerIntegrationTest {

    @Test
    fun `real compile errors are parsed with their position`() {
        assumeVectorAvailable()

        val stderr = VRLCompilerDiagnosticsRunner.run("vector", "x = parse_json(.message)\ny = upcase(x)")
        assertTrue("expected non-null stderr", stderr != null)

        val diagnostics = VRLCompilerDiagnosticsParser.parse(stderr!!)
        assertEquals(2, diagnostics.size)
        assertEquals("E103", diagnostics[0].code)
        assertEquals(1, diagnostics[0].line)
        assertEquals("unhandled fallible assignment", diagnostics[0].title)
        assertEquals("E701", diagnostics[1].code)
        assertEquals(2, diagnostics[1].line)
    }

    @Test
    fun `a script that really compiles produces no diagnostics`() {
        assumeVectorAvailable()

        val stderr = VRLCompilerDiagnosticsRunner.run("vector", ".x = upcase(\"hello\")")
        assertTrue("expected non-null stderr", stderr != null)
        assertEquals(emptyList<VRLCompilerDiagnostic>(), VRLCompilerDiagnosticsParser.parse(stderr!!))
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
