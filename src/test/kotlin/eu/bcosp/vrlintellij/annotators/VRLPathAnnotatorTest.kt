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
}
