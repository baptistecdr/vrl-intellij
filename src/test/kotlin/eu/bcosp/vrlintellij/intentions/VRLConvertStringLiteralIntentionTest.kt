package eu.bcosp.vrlintellij.intentions

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Each conversion below was run through `vector vrl` 0.58.0 in both forms first, comparing the
 * evaluated value rather than just the text - so these assertions pin "means the same thing",
 * not merely "was rewritten".
 */
class VRLConvertStringLiteralIntentionTest : BasePlatformTestCase() {

    private val toRaw = "Convert to raw string s'...'"
    private val toInterpreted = "Convert to interpreted string \"...\""

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

    fun testConvertsPlainInterpretedStringToRaw() {
        applyIntention(".a = \"a<caret>bc\"\n", toRaw, ".a = s'abc'\n")
    }

    fun testConvertsPlainRawStringToInterpreted() {
        applyIntention(".a = s'a<caret>bc'\n", toInterpreted, ".a = \"abc\"\n")
    }

    // The double quote needs escaping on the way in, and unescaping on the way out.
    fun testConvertsEscapedDoubleQuoteToRaw() {
        applyIntention(".a = \"a<caret>\\\"b\"\n", toRaw, ".a = s'a\"b'\n")
    }

    fun testConvertsRawDoubleQuoteToInterpreted() {
        applyIntention(".a = s'a<caret>\"b'\n", toInterpreted, ".a = \"a\\\"b\"\n")
    }

    fun testConvertsEscapedBackslashToRaw() {
        applyIntention(".a = \"a<caret>\\\\b\"\n", toRaw, ".a = s'a\\b'\n")
    }

    fun testConvertsRawBackslashToInterpreted() {
        applyIntention(".a = s'a<caret>\\b'\n", toInterpreted, ".a = \"a\\\\b\"\n")
    }

    // An escaped `\{{` is already literal, so a raw string says the same thing.
    fun testConvertsEscapedInterpolationToRaw() {
        applyIntention(".a = \"got <caret>\\{{x}}\"\n", toRaw, ".a = s'got {{x}}'\n")
    }

    // Going the other way the braces must be re-escaped, or the result would start interpolating.
    fun testEscapesBracesWhenConvertingRawToInterpreted() {
        applyIntention(".a = s'got <caret>{{x}}'\n", toInterpreted, ".a = \"got \\{{x}}\"\n")
    }

    // `"got {{x}}"` evaluates to `got V` when x is "V"; `s'got {{x}}'` evaluates to `got {{x}}`.
    // There is no raw string with the same meaning, so the intention stays away.
    fun testDoesNotOfferRawConversionForInterpolatedString() {
        assertFalse(isOffered(".a = \"got <caret>{{x}}\"\n", toRaw))
    }

    // `s'a'b'` and `s'a''b'` are both compiler error 203 - a raw string can't hold a quote at all.
    fun testDoesNotOfferRawConversionForStringContainingSingleQuote() {
        assertFalse(isOffered(".a = \"it<caret>'s\"\n", toRaw))
    }

    // A raw string has no escapes, so `\t` could only survive as a literal tab in the source.
    fun testDoesNotOfferRawConversionForControlCharacterEscape() {
        assertFalse(isOffered(".a = \"a<caret>\\tb\"\n", toRaw))
    }

    fun testDoesNotOfferOnRegexLiteral() {
        assertFalse(isOffered(".a = r'a<caret>bc'\n", toRaw))
        assertFalse(isOffered(".a = r'a<caret>bc'\n", toInterpreted))
    }

    fun testDoesNotOfferOnNonStringLiteral() {
        assertFalse(isOffered(".a = 1<caret>23\n", toRaw))
        assertFalse(isOffered(".a = 1<caret>23\n", toInterpreted))
    }

    fun testRoundTripsBackToTheOriginal() {
        applyIntention(".a = \"a<caret>\\\"b\"\n", toRaw, ".a = s'a\"b'\n")
        myFixture.configureByText("t.vrl", ".a = s'a<caret>\"b'\n")
        val back = myFixture.getAvailableIntention(toInterpreted)
        assertNotNull(back)
        myFixture.launchAction(back!!)
        myFixture.checkResult(".a = \"a\\\"b\"\n")
    }
}
