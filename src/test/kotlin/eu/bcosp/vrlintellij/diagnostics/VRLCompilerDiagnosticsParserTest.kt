package eu.bcosp.vrlintellij.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VRLCompilerDiagnosticsParserTest {

    // Built at runtime (never as a literal escape byte or a "[<digits>m" substring in this
    // source file) so a real color-coded sample can be exercised without anything here looking
    // like an actual escape sequence embedded in the file itself.
    private val esc = 0x1B.toChar()
    private fun sgr(code: String) = "$esc[${code}m"

    @Test
    fun `parses a single syntax error`() {
        val stderr = """
            error[E203]: syntax error
              ┌─ :1:11
              │
            1 │ .x = .foo["bar"]
              │           ^^^^^
              │           │
              │           unexpected syntax token: "StringLiteral"
              │           expected one of: "integer literal"
              │
              = see language documentation at https://vrl.dev
              = try your code in the VRL REPL, learn more at https://vrl.dev/examples

        """.trimIndent()

        val diagnostics = VRLCompilerDiagnosticsParser.parse(stderr)

        assertEquals(1, diagnostics.size)
        val diagnostic = diagnostics.single()
        assertEquals(1, diagnostic.line)
        assertEquals(11, diagnostic.column)
        assertEquals("E203", diagnostic.code)
        assertEquals("syntax error", diagnostic.title)
        assertTrue(diagnostic.detail.contains("unexpected syntax token"))
        assertTrue(diagnostic.detail.contains("expected one of"))
    }

    @Test
    fun `parses multiple diagnostics from one run`() {
        val stderr = """
            error[E651]: unnecessary error coalescing operation
              ┌─ :1:5
              │
            1 │ y = .foo ?? "bar"
              │     ^^^^ -- ----- this expression never resolves
              │     │    │
              │     │    remove this error coalescing operation
              │     this expression can't fail
              │
              = see language documentation at https://vrl.dev

            error[E203]: syntax error
              ┌─ :2:11
              │
            2 │ .x = .baz["qux"]
              │           ^^^^^
              │           │
              │           unexpected syntax token: "StringLiteral"
              │           expected one of: "integer literal"
              │
              = see language documentation at https://vrl.dev

        """.trimIndent()

        val diagnostics = VRLCompilerDiagnosticsParser.parse(stderr)

        assertEquals(2, diagnostics.size)
        assertEquals("E651", diagnostics[0].code)
        assertEquals(1, diagnostics[0].line)
        assertEquals(5, diagnostics[0].column)
        assertEquals("E203", diagnostics[1].code)
        assertEquals(2, diagnostics[1].line)
        assertEquals(11, diagnostics[1].column)
    }

    @Test
    fun `strips color codes before parsing`() {
        val stderr = "${sgr("1")}${sgr("38;5;9")}error[E203]${sgr("0")}${sgr("1")}: syntax error${sgr("0")}\n" +
            "  ${sgr("34")}┌─${sgr("0")} :1:11\n" +
            "  ${sgr("34")}│${sgr("0")}\n" +
            "1 ${sgr("34")}│${sgr("0")} .x = .foo[${sgr("31")}\"bar\"${sgr("0")}]\n" +
            "  ${sgr("34")}│${sgr("0")}           ${sgr("31")}^^^^^${sgr("0")}\n" +
            "  ${sgr("34")}│${sgr("0")}           ${sgr("31")}│${sgr("0")}\n" +
            "  ${sgr("34")}│${sgr("0")}           ${sgr("31")}unexpected syntax token: \"StringLiteral\"${sgr("0")}\n" +
            "  ${sgr("34")}│${sgr("0")}\n" +
            "  ${sgr("34")}=${sgr("0")} see language documentation at https://vrl.dev\n"

        val diagnostics = VRLCompilerDiagnosticsParser.parse(stderr)

        assertEquals(1, diagnostics.size)
        val diagnostic = diagnostics.single()
        assertEquals(1, diagnostic.line)
        assertEquals(11, diagnostic.column)
        assertEquals("E203", diagnostic.code)
        assertEquals("syntax error", diagnostic.title)
        assertTrue(diagnostic.detail.contains("unexpected syntax token"))
    }

    @Test
    fun `returns nothing for clean stderr`() {
        assertEquals(emptyList<VRLCompilerDiagnostic>(), VRLCompilerDiagnosticsParser.parse(""))
    }

    @Test
    fun `ignores a runtime evaluation error, not a compile diagnostic`() {
        // What "vector vrl" prints when a raise-on-error call fails against the synthetic {}
        // event this plugin evaluates against - see VRLCompilerDiagnosticsRunner. This must never
        // be surfaced as a compile diagnostic: it's an artifact of the missing real input, not a
        // defect in the script.
        val stderr = "function call error for \"upcase\" at (1:5): expected string, got null\n"

        assertEquals(emptyList<VRLCompilerDiagnostic>(), VRLCompilerDiagnosticsParser.parse(stderr))
    }
}
