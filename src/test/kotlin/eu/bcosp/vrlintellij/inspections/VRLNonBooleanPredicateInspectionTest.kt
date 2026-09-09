package eu.bcosp.vrlintellij.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Every case here was run through `vector vrl` 0.58.0 first: the flagged ones report
 * `error[E102]: non-boolean predicate`, the unflagged ones compile.
 */
class VRLNonBooleanPredicateInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(VRLNonBooleanPredicateInspection())
    }

    private fun isFlagged(text: String): Boolean {
        myFixture.configureByText("t.vrl", text)
        return myFixture.doHighlighting().any { it.description?.contains("Non-boolean predicate") == true }
    }

    fun testFlagsStringLiteralPredicate() {
        assertTrue(isFlagged("if \"a\" { 1 } else { 2 }\n"))
    }

    fun testFlagsIntegerLiteralPredicate() {
        assertTrue(isFlagged("if 1 { 1 } else { 2 }\n"))
    }

    fun testFlagsNullLiteralPredicate() {
        assertTrue(isFlagged("if null { 1 } else { 2 }\n"))
    }

    fun testFlagsArrayLiteralPredicate() {
        assertTrue(isFlagged("if [1] { 1 } else { 2 }\n"))
    }

    fun testFlagsTimestampLiteralPredicate() {
        assertTrue(isFlagged("if t'2021-01-01T00:00:00Z' { 1 } else { 2 }\n"))
    }

    fun testReportsTheResolvedTypeInTheMessage() {
        myFixture.configureByText("t.vrl", "if \"a\" { 1 } else { 2 }\n")
        val messages = myFixture.doHighlighting().mapNotNull { it.description }
        assertTrue(messages.any { it.contains("resolves to string") })
    }

    fun testDoesNotFlagBooleanLiterals() {
        assertFalse(isFlagged("if true { 1 } else { 2 }\n"))
        assertFalse(isFlagged("if false { 1 } else { 2 }\n"))
    }

    fun testDoesNotFlagComparison() {
        assertFalse(isFlagged("if 1 == 1 { 1 } else { 2 }\n"))
    }

    fun testDoesNotFlagBooleanReturningCall() {
        assertFalse(isFlagged("if is_string(\"a\") { 1 } else { 2 }\n"))
    }

    // VRL narrows path types through assignment: `if .foo { }` alone is E102, but the same line
    // compiles after `.foo = true`. Flagging paths on sight would warn about working code, so
    // predicates are only judged when they're literals.
    fun testDoesNotFlagPathPredicate() {
        assertFalse(isFlagged("if .foo { 1 } else { 2 }\n"))
    }

    fun testDoesNotFlagVariablePredicate() {
        assertFalse(isFlagged("b = \"a\"\nif b { 1 } else { 2 }\n"))
    }

    fun testFlagsPredicateOfElseIfBranch() {
        assertTrue(isFlagged("if true { 1 } else if \"a\" { 2 } else { 3 }\n"))
    }
}
