package eu.bcosp.vrlintellij.formatting

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

// Verified against the real vector vrl CLI (VECTOR_LOG=off vector vrl -p script.vrl): a trailing
// comma right before ]/}/) is accepted in array/object literals and call argument lists - matches
// VRL.bnf's own `(COMMA x)* COMMA?` for all three.
class VRLTrailingCommaPostFormatProcessorTest : BasePlatformTestCase() {

    private fun reformat(text: String, trailingComma: Boolean): String {
        val settings = CodeStyle.getSettings(project).getCustomSettings(VRLCodeStyleSettings::class.java)
        val original = settings.TRAILING_COMMA
        settings.TRAILING_COMMA = trailingComma
        try {
            myFixture.configureByText("t.vrl", text)
            WriteCommandAction.runWriteCommandAction(project) {
                CodeStyleManager.getInstance(project).reformat(myFixture.file)
            }
            return myFixture.file.text
        } finally {
            settings.TRAILING_COMMA = original
        }
    }

    // reformat() also normalizes indentation (VRLBlock indents array/object elements one level -
    // see VRLLanguageCodeStyleSettingsProviderTest's own indent test), so expected values below
    // reflect that too, not just the trailing comma itself.

    fun testAddsTrailingCommaToMultiLineArrayWhenEnabled() {
        val result = reformat("x = [\n1,\n2,\n3\n]\n", trailingComma = true)
        assertEquals("x = [\n    1,\n    2,\n    3,\n]\n", result)
    }

    fun testAddsTrailingCommaToMultiLineObjectWhenEnabled() {
        val result = reformat("x = {\n\"a\": 1,\n\"b\": 2\n}\n", trailingComma = true)
        assertEquals("x = {\n    \"a\": 1,\n    \"b\": 2,\n}\n", result)
    }

    // Unlike array/object elements, call arguments aren't given any extra indent by VRLBlock, so
    // this one - alone among these tests - stays byte-for-byte aside from the trailing comma.
    fun testAddsTrailingCommaToMultiLineArgumentListWhenEnabled() {
        val result = reformat("split(\n\"a,b\",\npattern: \",\"\n)\n", trailingComma = true)
        assertEquals("split(\n\"a,b\",\npattern: \",\",\n)\n", result)
    }

    fun testLeavesAnAlreadyPresentTrailingCommaAloneWhenEnabled() {
        val result = reformat("x = [\n1,\n2,\n3,\n]\n", trailingComma = true)
        assertEquals("x = [\n    1,\n    2,\n    3,\n]\n", result)
    }

    fun testRemovesTrailingCommaFromMultiLineArrayWhenDisabled() {
        val result = reformat("x = [\n1,\n2,\n3,\n]\n", trailingComma = false)
        assertEquals("x = [\n    1,\n    2,\n    3\n]\n", result)
    }

    fun testDoesNotAddTrailingCommaToASingleLineArrayEvenWhenEnabled() {
        val result = reformat("x = [1, 2, 3]\n", trailingComma = true)
        assertEquals("x = [1, 2, 3]\n", result)
    }

    fun testRemovesTrailingCommaFromASingleLineArrayEvenWhenEnabled() {
        // A trailing comma someone typed on a single line is still not the multi-line style this
        // option means, so it's cleaned up regardless of the setting.
        val result = reformat("x = [1, 2, 3,]\n", trailingComma = true)
        assertEquals("x = [1, 2, 3]\n", result)
    }

    fun testDoesNotTouchAnEmptyMultiLineArray() {
        val result = reformat("x = [\n]\n", trailingComma = true)
        assertEquals("x = [\n]\n", result)
    }

    fun testHandlesNestedContainersIndependently() {
        val result = reformat("x = [\n[\n1,\n2\n],\n3\n]\n", trailingComma = true)
        assertEquals("x = [\n    [\n        1,\n        2,\n    ],\n    3,\n]\n", result)
    }
}
