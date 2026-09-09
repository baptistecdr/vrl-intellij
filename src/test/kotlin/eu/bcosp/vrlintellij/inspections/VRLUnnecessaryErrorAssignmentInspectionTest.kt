package eu.bcosp.vrlintellij.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Every case here was run through `vector vrl` 0.58.0 first: the flagged ones report
 * `error[E104]: unnecessary error assignment`, the unflagged ones compile.
 */
class VRLUnnecessaryErrorAssignmentInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(VRLUnnecessaryErrorAssignmentInspection())
    }

    private fun isFlagged(text: String): Boolean {
        myFixture.configureByText("t.vrl", text)
        return myFixture.doHighlighting().any { it.description?.contains("Unnecessary error assignment") == true }
    }

    fun testFlagsRaisedCall() {
        assertTrue(isFlagged("v, err = parse_json!(\"{}\")\nv\n"))
    }

    fun testFlagsStringLiteral() {
        assertTrue(isFlagged("v, err = \"lit\"\nv\n"))
    }

    fun testFlagsObjectLiteral() {
        assertTrue(isFlagged("v, err = {\"a\": 1}\nv\n"))
    }

    fun testFlagsBooleanLiteral() {
        assertTrue(isFlagged("v, err = true\nv\n"))
    }

    // `??` has already consumed the error, so nothing is left to destructure.
    fun testFlagsCoalescedExpression() {
        assertTrue(isFlagged("v, err = parse_json(\"{}\") ?? {}\nv\n"))
    }

    fun testDoesNotFlagFallibleCall() {
        assertFalse(isFlagged("v, err = parse_json(\"{}\")\nv\n"))
    }

    // Raising inside an argument doesn't stop the outer call from failing - `vector vrl` accepts
    // this exact program, so only an outermost `!` counts.
    fun testDoesNotFlagCallWithRaisedArgument() {
        assertFalse(isFlagged("v, err = parse_json(string!(.a))\nv\n"))
    }

    // Also rejected by `vector vrl`, but proving it needs the drifting fallibility metadata.
    fun testDoesNotFlagPath() {
        assertFalse(isFlagged("v, err = .foo\nv\n"))
    }

    fun testDoesNotFlagInfallibleCall() {
        assertFalse(isFlagged("v, err = upcase(\"a\")\nv\n"))
    }

    fun testDoesNotFlagSingleAssignment() {
        assertFalse(isFlagged("v = \"lit\"\nv\n"))
    }
}
