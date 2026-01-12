package eu.bcosp.vrlintellij.annotators

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import eu.bcosp.vrlintellij.colors.VRLColor

class VRLNamedArgumentAnnotatorTest : BasePlatformTestCase() {

    fun testColorsNamedArgumentName() {
        myFixture.configureByText("t.vrl", "split(\"a,b\", pattern: \",\")")
        val highlights = myFixture.doHighlighting()
        val patternStart = myFixture.file.text.indexOf("pattern")
        assertTrue(highlights.any {
            it.forcedTextAttributesKey == VRLColor.NAMED_ARGUMENTS.textAttributesKey && it.startOffset == patternStart
        })
    }

    fun testDoesNotColorPositionalArgument() {
        myFixture.configureByText("t.vrl", "split(\"a,b\", \",\")")
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.forcedTextAttributesKey == VRLColor.NAMED_ARGUMENTS.textAttributesKey })
    }
}
