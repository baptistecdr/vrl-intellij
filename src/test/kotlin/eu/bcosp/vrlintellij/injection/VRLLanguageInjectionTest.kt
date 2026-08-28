package eu.bcosp.vrlintellij.injection

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import eu.bcosp.vrlintellij.psi.VRLLiteral

class VRLLanguageInjectionTest : BasePlatformTestCase() {

    private fun literalAt(text: String, needle: String): VRLLiteral {
        myFixture.configureByText("t.vrl", text)
        val literals = PsiTreeUtil.findChildrenOfType(myFixture.file, VRLLiteral::class.java)
        return literals.first { it.text.contains(needle) }
    }

    private fun decode(host: PsiLanguageInjectionHost): String {
        val escaper = host.createLiteralTextEscaper()
        val outChars = StringBuilder()
        escaper.decode(escaper.relevantTextRange, outChars)
        return outChars.toString()
    }

    fun testStringLiteralIsAValidInjectionHost() {
        val literal = literalAt("x = \"hello\"\n", "hello")
        assertTrue((literal as PsiLanguageInjectionHost).isValidHost)
    }

    fun testRawStringLiteralIsAValidInjectionHost() {
        val literal = literalAt("x = s'hello'\n", "hello")
        assertTrue((literal as PsiLanguageInjectionHost).isValidHost)
    }

    fun testRegexLiteralIsAValidInjectionHost() {
        val literal = literalAt("x = r'^foo$'\n", "foo")
        assertTrue((literal as PsiLanguageInjectionHost).isValidHost)
    }

    fun testNumberLiteralIsNotAValidInjectionHost() {
        val literal = literalAt("x = 123\n", "123")
        assertFalse((literal as PsiLanguageInjectionHost).isValidHost)
    }

    fun testStringEscaperDecodesStandardEscapeSequences() {
        val literal = literalAt("x = \"a\\nb\\tc\\\\d\\\"e\"\n", "a\\n")
        assertEquals("a\nb\tc\\d\"e", decode(literal as PsiLanguageInjectionHost))
    }

    fun testStringEscaperDecodesUnicodeEscapes() {
        val literal = literalAt("x = \"\\u{48}\\u{65}\\u{6c}\\u{6c}\\u{6f}\"\n", "48")
        assertEquals("Hello", decode(literal as PsiLanguageInjectionHost))
    }

    fun testRawStringEscaperAppliesNoEscapeProcessing() {
        val literal = literalAt("x = s'C:\\\\foo\\nbar'\n", "foo")
        assertEquals("C:\\\\foo\\nbar", decode(literal as PsiLanguageInjectionHost))
    }

    fun testStringEscaperOffsetInHostStaysWithinRelevantRange() {
        val literal = literalAt("x = \"a\\nb\"\n", "a\\n")
        val host = literal as PsiLanguageInjectionHost
        val escaper = host.createLiteralTextEscaper()
        val relevant = escaper.relevantTextRange
        val decoded = StringBuilder()
        escaper.decode(relevant, decoded)
        for (i in 0..decoded.length) {
            val hostOffset = escaper.getOffsetInHost(i, relevant)
            assertTrue(
                "offset $hostOffset for decoded index $i out of range $relevant",
                hostOffset in relevant.startOffset..relevant.endOffset
            )
        }
    }

    fun testStringEscaperRelevantTextRangeExcludesQuotes() {
        val literal = literalAt("x = \"hello\"\n", "hello")
        val host = literal as PsiLanguageInjectionHost
        val relevant = host.createLiteralTextEscaper().relevantTextRange
        assertEquals(TextRange(1, literal.text.length - 1), relevant)
    }
}
