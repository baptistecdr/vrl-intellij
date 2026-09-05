package eu.bcosp.vrlintellij.moveUpDown

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLStatementUpDownMoverTest : BasePlatformTestCase() {

    private fun move(text: String, down: Boolean): String {
        myFixture.configureByText("t.vrl", text)
        myFixture.performEditorAction(if (down) "MoveStatementDown" else "MoveStatementUp")
        return myFixture.file.text
    }

    fun `test moves a single-line statement down`() {
        val result = move("x = <caret>1;\ny = 2;\n", down = true)
        assertEquals("y = 2;\nx = 1;\n", result)
    }

    fun `test moves a single-line statement up`() {
        val result = move("x = 1;\ny = <caret>2;\n", down = false)
        assertEquals("y = 2;\nx = 1;\n", result)
    }

    fun `test moves a whole multi-line if block down without tearing it apart`() {
        val result = move("if <caret>true {\n    x = 1;\n    x = 2;\n}\ny = 3;\n", down = true)
        assertEquals("y = 3;\nif true {\n    x = 1;\n    x = 2;\n}\n", result)
    }

    fun `test moves a whole multi-line if block up without tearing it apart`() {
        val result = move("y = 3;\nif <caret>true {\n    x = 1;\n    x = 2;\n}\n", down = false)
        assertEquals("if true {\n    x = 1;\n    x = 2;\n}\ny = 3;\n", result)
    }

    fun `test does nothing when already the last statement`() {
        val result = move("x = 1;\ny = <caret>2;\n", down = true)
        assertEquals("x = 1;\ny = 2;\n", result)
    }

    fun `test does nothing when already the first statement`() {
        val result = move("x = <caret>1;\ny = 2;\n", down = false)
        assertEquals("x = 1;\ny = 2;\n", result)
    }

    fun `test moves statements inside a block, not out of it`() {
        val result = move("if true {\n    x = <caret>1;\n    y = 2;\n}\n", down = true)
        assertEquals("if true {\n    y = 2;\n    x = 1;\n}\n", result)
    }
}
