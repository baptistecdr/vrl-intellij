package eu.bcosp.vrlintellij.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Every case here was run through `vector vrl` 0.58.0 first: the flagged ones report
 * `error[E651]: unnecessary error coalescing operation`, the unflagged ones compile.
 */
class VRLUnnecessaryErrorCoalescingInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(VRLUnnecessaryErrorCoalescingInspection())
    }

    private fun isFlagged(text: String): Boolean {
        myFixture.configureByText("t.vrl", text)
        return myFixture.doHighlighting().any { it.description?.contains("Unnecessary error coalescing") == true }
    }

    fun testFlagsStringLiteralLeftOperand() {
        assertTrue(isFlagged("x = \"lit\" ?? \"fallback\"\n"))
    }

    fun testFlagsIntegerLiteralLeftOperand() {
        assertTrue(isFlagged("x = 1 ?? 2\n"))
    }

    fun testFlagsObjectLiteralLeftOperand() {
        assertTrue(isFlagged("x = {\"a\": 1} ?? {}\n"))
    }

    // Raising already turns the error into an abort, so nothing is left for `??` to catch.
    fun testFlagsRaisedCallLeftOperand() {
        assertTrue(isFlagged("x = parse_json!(\"{}\") ?? {}\n"))
    }

    fun testDoesNotFlagFallibleCall() {
        assertFalse(isFlagged("x = parse_json(\"{}\") ?? {}\n"))
    }

    // A chain parses as one node with three operands, and each `??` needs the accumulated left
    // side to still be fallible - `vector vrl` accepts this exact program because both
    // parse_json calls can fail.
    fun testDoesNotFlagChainWhereEveryCoalesceIsJustified() {
        assertFalse(isFlagged("x = parse_json(\"{}\") ?? parse_json(\"{}\") ?? {}\n"))
    }

    // Known limitation, and a deliberate one: `vector vrl` reports E651 here (after the first
    // `??` the value can no longer fail, so the second one is redundant), but this inspection
    // only judges the first operand. Under-reporting keeps the chain logic honest rather than
    // re-deriving VRL's left-associative fallibility folding.
    fun testDoesNotFlagRedundantSecondCoalesceInAChain() {
        assertFalse(isFlagged("x = parse_json(\"{}\") ?? {} ?? {}\n"))
    }

    // `vector vrl` does reject these (paths and infallible calls can't fail either), but proving
    // that needs the per-function fallibility metadata, which is known to drift - see
    // isStructurallyInfallible. Missing them is the deliberate trade.
    fun testDoesNotFlagPathLeftOperand() {
        assertFalse(isFlagged("x = .foo ?? \"fallback\"\n"))
    }

    fun testDoesNotFlagBareCallLeftOperand() {
        assertFalse(isFlagged("x = upcase(\"a\") ?? \"fallback\"\n"))
    }

    fun testDoesNotFlagExpressionWithoutCoalescing() {
        assertFalse(isFlagged("x = \"lit\"\n"))
    }

    // getAvailableIntention only offers fixes whose problem covers the caret, and this warning
    // sits on the left operand rather than at the start of the line - hence the explicit caret.
    fun testQuickFixRemovesTheFallback() {
        myFixture.configureByText("t.vrl", "x = parse_json<caret>!(\"{}\") ?? {}\n")
        val intention = myFixture.getAvailableIntention("Remove '??' fallback")
        assertNotNull(intention)
        myFixture.launchAction(intention!!)
        myFixture.checkResult("x = parse_json!(\"{}\")\n")
    }

    fun testQuickFixRemovesEveryLinkOfAChain() {
        myFixture.configureByText("t.vrl", "x = <caret>1 ?? 2 ?? 3\n")
        val intention = myFixture.getAvailableIntention("Remove '??' fallback")
        assertNotNull(intention)
        myFixture.launchAction(intention!!)
        myFixture.checkResult("x = 1\n")
    }
}
