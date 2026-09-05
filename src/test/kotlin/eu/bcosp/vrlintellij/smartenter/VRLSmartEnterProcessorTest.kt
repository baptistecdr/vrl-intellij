package eu.bcosp.vrlintellij.smartenter

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLSmartEnterProcessorTest : BasePlatformTestCase() {

    private fun smartEnter(text: String): String {
        myFixture.configureByText("t.vrl", text)
        myFixture.performEditorAction("EditorCompleteStatement")
        return myFixture.file.text
    }

    fun `test completes an if with no block`() {
        val result = smartEnter("if true<caret>\n")
        assertEquals("if true {\n    \n}\n", result)
    }

    fun `test places the caret inside the new block`() {
        myFixture.configureByText("t.vrl", "if true<caret>\n")
        myFixture.performEditorAction("EditorCompleteStatement")

        val text = myFixture.file.text
        assertEquals("if true {\n    \n}\n", text)
        val expectedCaretOffset = text.indexOf("    \n") + "    ".length
        assertEquals(expectedCaretOffset, myFixture.editor.caretModel.offset)
    }

    fun `test caret position inside the line does not matter`() {
        val result = smartEnter("if <caret>true\n")
        assertEquals("if true {\n    \n}\n", result)
    }

    fun `test does nothing when the if already has a block`() {
        val result = smartEnter("if true {<caret>\n    x = 1;\n}\n")
        assertEquals("if true {\n\n    x = 1;\n}\n", result)
    }

    fun `test does nothing on a line that is not an if`() {
        val result = smartEnter("x = 1<caret>\n")
        assertEquals("x = 1\n\n", result)
    }

    fun `test does not trigger on an identifier that merely starts with if`() {
        val result = smartEnter("ifoo = true<caret>\n")
        assertEquals("ifoo = true\n\n", result)
    }
}
