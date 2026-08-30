package eu.bcosp.vrlintellij.annotators

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import eu.bcosp.vrlintellij.colors.VRLColor

class VRLClosureParameterAnnotatorTest : BasePlatformTestCase() {

    fun testColorsClosureParameterDeclaration() {
        myFixture.configureByText("t.vrl", "map_values(.) -> |v| {\nv\n}\n")
        val highlights = myFixture.doHighlighting()
        val declOffset = myFixture.file.text.indexOf("|v|") + 1
        assertTrue(highlights.any {
            it.forcedTextAttributesKey == VRLColor.CLOSURE_PARAMETER.textAttributesKey && it.startOffset == declOffset
        })
    }

    fun testColorsClosureParameterUsageInsideBody() {
        myFixture.configureByText("t.vrl", "for_each(.) -> |k, v| {\nx = v\n}\n")
        val highlights = myFixture.doHighlighting()
        val usageOffset = myFixture.file.text.lastIndexOf("v")
        assertTrue(highlights.any {
            it.forcedTextAttributesKey == VRLColor.CLOSURE_PARAMETER.textAttributesKey && it.startOffset == usageOffset
        })
    }

    fun testDoesNotColorPlainVariable() {
        myFixture.configureByText("t.vrl", "x = 1\ny = x\n")
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.forcedTextAttributesKey == VRLColor.CLOSURE_PARAMETER.textAttributesKey })
    }

    fun testDoesNotColorOuterVariableSharingAClosureParamNameFromAnotherClosure() {
        // `k` (the actual closure param) is expected to be colored - only `v`, a plain outer
        // variable that happens to share a name with no param in *this* closure, must not be.
        myFixture.configureByText("t.vrl", "v = 1\nmap_values(.) -> |k| {\nx = v\n}\n")
        val highlights = myFixture.doHighlighting()
        val vUsageOffset = myFixture.file.text.lastIndexOf("v")
        assertTrue(highlights.none {
            it.forcedTextAttributesKey == VRLColor.CLOSURE_PARAMETER.textAttributesKey && it.startOffset == vUsageOffset
        })
    }
}
