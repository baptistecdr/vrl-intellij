package eu.bcosp.vrlintellij.intentions

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * The before/after shapes here were all accepted by `vector vrl` 0.58.0, including the path-target
 * forms (`.a = parse_json!("{}")` and `.a, err = parse_json("{}")`).
 */
class VRLErrorHandlingIntentionsTest : BasePlatformTestCase() {

    private val toDestructuring = "Replace '!' with error destructuring"
    private val toRaise = "Replace error destructuring with '!'"

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

    fun testConvertsRaisedVariableAssignment() {
        applyIntention(
            "x = parse_json<caret>!(\"{}\")\nx\n",
            toDestructuring,
            "x, err = parse_json(\"{}\")\nx\n",
        )
    }

    fun testConvertsRaisedPathAssignment() {
        applyIntention(
            ".a = parse_json<caret>!(\"{}\")\n.a\n",
            toDestructuring,
            ".a, err = parse_json(\"{}\")\n.a\n",
        )
    }

    // Shadowing a live `err` would change what a later `if err != null` reads.
    fun testPicksAFreeNameWhenErrIsTaken() {
        applyIntention(
            "v, err = parse_json(\"{}\")\nif err != null { v = {} }\nx = parse_json<caret>!(\"{}\")\nx\n",
            toDestructuring,
            "v, err = parse_json(\"{}\")\nif err != null { v = {} }\nx, err1 = parse_json(\"{}\")\nx\n",
        )
    }

    fun testDoesNotOfferDestructuringForUnraisedCall() {
        assertFalse(isOffered("x = parse_json<caret>(\"{}\")\nx\n", toDestructuring))
    }

    fun testConvertsUnreadErrorTargetBackToRaise() {
        applyIntention(
            "x, err = parse_json<caret>(\"{}\")\nx\n",
            toRaise,
            "x = parse_json!(\"{}\")\nx\n",
        )
    }

    fun testConvertsPathTargetBackToRaise() {
        applyIntention(
            ".a, err = parse_json<caret>(\"{}\")\n.a\n",
            toRaise,
            ".a = parse_json!(\"{}\")\n.a\n",
        )
    }

    // Removing `err` here would leave `if err != null` referring to nothing.
    fun testDoesNotOfferRaiseWhenErrorIsRead() {
        assertFalse(
            isOffered("x, err = parse_json<caret>(\"{}\")\nif err != null {\n    x = {}\n}\nx\n", toRaise),
        )
    }

    // `_ = f!()` is compiler error 640, so there's no single-assignment form to convert into.
    fun testDoesNotOfferRaiseForDiscardedValueTarget() {
        assertFalse(isOffered("_, err = parse_json<caret>(\"{}\")\n1\n", toRaise))
    }

    fun testDoesNotOfferRaiseWhenAlreadyRaising() {
        assertFalse(isOffered("x, err = parse_json<caret>!(\"{}\")\nx\n", toRaise))
    }

    // Raising inside an argument doesn't make the outer call infallible, so the outer call still
    // needs its error handled and the intention must not treat it as already raising.
    fun testDoesNotOfferDestructuringForNestedRaiseOnly() {
        assertFalse(isOffered("x = parse_json<caret>(string!(.a))\nx\n", toDestructuring))
    }

    fun testRoundTripsBackToTheOriginal() {
        applyIntention("x = parse_json<caret>!(\"{}\")\nx\n", toDestructuring, "x, err = parse_json(\"{}\")\nx\n")
        myFixture.configureByText("t.vrl", "x, err = parse_json<caret>(\"{}\")\nx\n")
        val back = myFixture.getAvailableIntention(toRaise)
        assertNotNull(back)
        myFixture.launchAction(back!!)
        myFixture.checkResult("x = parse_json!(\"{}\")\nx\n")
    }
}
