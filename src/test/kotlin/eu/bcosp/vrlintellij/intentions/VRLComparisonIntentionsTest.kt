package eu.bcosp.vrlintellij.intentions

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLComparisonIntentionsTest : BasePlatformTestCase() {

    private fun applyIntention(text: String, name: String, expected: String) {
        myFixture.configureByText("t.vrl", text)
        val intention = myFixture.getAvailableIntention(name)
        assertNotNull("intention '$name' should be offered for: $text", intention)
        myFixture.launchAction(intention!!)
        myFixture.checkResult(expected)
    }

    private fun isOffered(text: String, name: String): Boolean {
        myFixture.configureByText("t.vrl", text)
        return myFixture.getAvailableIntention(name) != null
    }

    fun testFlipSwapsOperandsAndMirrorsGreaterThan() {
        applyIntention(".a = .status <caret>> 200\n", "Flip '>' to '<'", ".a = 200 < .status\n")
    }

    fun testFlipMirrorsGreaterOrEqual() {
        applyIntention(".a = .status <caret>>= 200\n", "Flip '>=' to '<='", ".a = 200 <= .status\n")
    }

    fun testFlipKeepsEqualityOperator() {
        applyIntention(".a = .status <caret>== 200\n", "Flip '==' to '=='", ".a = 200 == .status\n")
    }

    fun testFlipKeepsInequalityOperator() {
        applyIntention(".a = .status <caret>!= 200\n", "Flip '!=' to '!='", ".a = 200 != .status\n")
    }

    fun testNegateInvertsGreaterOrEqual() {
        applyIntention(".a = .status <caret>>= 400\n", "Negate '>=' to '<'", ".a = .status < 400\n")
    }

    fun testNegateInvertsEquality() {
        applyIntention(".a = .status <caret>== 200\n", "Negate '==' to '!='", ".a = .status != 200\n")
    }

    fun testNegateInvertsLessThan() {
        applyIntention(".a = .status <caret>< 200\n", "Negate '<' to '>='", ".a = .status >= 200\n")
    }

    fun testWorksInsideAnIfCondition() {
        applyIntention("if .status <caret>> 200 {\n    .a = 1\n}\n", "Negate '>' to '<='", "if .status <= 200 {\n    .a = 1\n}\n")
    }

    // The grammar's comparison rule also matches a bare operand, so an expression with no
    // comparison operator must not offer either intention.
    fun testNotOfferedWithoutAComparisonOperator() {
        assertFalse(isOffered(".a = <caret>200\n", "Flip comparison"))
        assertFalse(isOffered(".a = <caret>200\n", "Negate comparison"))
    }

    fun testNotOfferedOnArithmetic() {
        assertFalse(isOffered(".a = 1 <caret>+ 2\n", "Flip comparison"))
    }
}
