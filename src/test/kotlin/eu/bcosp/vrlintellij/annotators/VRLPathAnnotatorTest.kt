package eu.bcosp.vrlintellij.annotators

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import eu.bcosp.vrlintellij.colors.VRLColor

class VRLPathAnnotatorTest : BasePlatformTestCase() {

    fun testColorsEventFieldPathSegment() {
        myFixture.configureByText("t.vrl", "del(.foo)")
        val highlights = myFixture.doHighlighting()
        val fooStart = myFixture.file.text.indexOf("foo")
        assertTrue(highlights.any {
            it.forcedTextAttributesKey == VRLColor.PATH.textAttributesKey && it.startOffset == fooStart
        })
    }

    fun testColorsChainedMemberAccess() {
        myFixture.configureByText("t.vrl", "x = get!(y, [\"z\"]).bar")
        val highlights = myFixture.doHighlighting()
        val barStart = myFixture.file.text.indexOf("bar")
        assertTrue(highlights.any {
            it.forcedTextAttributesKey == VRLColor.PATH.textAttributesKey && it.startOffset == barStart
        })
    }

    fun testDoesNotColorPlainVariable() {
        myFixture.configureByText("t.vrl", "x = 1;\ny = x;")
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.forcedTextAttributesKey == VRLColor.PATH.textAttributesKey })
    }

    fun testDoesNotColorFunctionCallName() {
        myFixture.configureByText("t.vrl", "upcase(\"x\")")
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.forcedTextAttributesKey == VRLColor.PATH.textAttributesKey })
    }

    private fun pathColoredAt(text: String, needle: String): Boolean {
        myFixture.configureByText("t.vrl", text)
        val start = myFixture.file.text.indexOf(needle)
        return myFixture.doHighlighting().any {
            it.forcedTextAttributesKey == VRLColor.PATH.textAttributesKey && it.startOffset == start
        }
    }

    // Keywords, reserved words and `@` names are valid field names (vrl's parser.lalrpop Field)
    // and must be colored as fields there, not keep their keyword color.
    fun testColorsKeywordAndReservedWordFields() {
        assertTrue(pathColoredAt(".if = 1\n", "if"))
        assertTrue(pathColoredAt(".for = 1\n", "for"))
        assertTrue(pathColoredAt("x = {}\n.y = x.string\n", "string"))
    }

    fun testColorsAtSignField() {
        assertTrue(pathColoredAt(".@timestamp = 1\n", "@timestamp"))
    }

    fun testColorsQuotedField() {
        assertTrue(pathColoredAt(".\"my key\" = 1\n", "\"my key\""))
    }
}
