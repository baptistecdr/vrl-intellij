package eu.bcosp.vrlintellij.surround

import com.intellij.codeInsight.generation.surroundWith.SurroundWithHandler
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLStatementsSurroundDescriptorTest : BasePlatformTestCase() {

    private fun surround(text: String, surrounder: com.intellij.lang.surroundWith.Surrounder): String {
        myFixture.configureByText("t.vrl", text)
        WriteCommandAction.runWriteCommandAction(project) {
            SurroundWithHandler.doSurround(project, myFixture.editor, surrounder, elementsToSurround())
        }
        return myFixture.file.text
    }

    private fun elementsToSurround(): Array<com.intellij.psi.PsiElement> {
        val selectionModel = myFixture.editor.selectionModel
        val descriptor = VRLStatementsSurroundDescriptor()
        val start = if (selectionModel.hasSelection()) selectionModel.selectionStart else myFixture.editor.caretModel.offset
        val end = if (selectionModel.hasSelection()) selectionModel.selectionEnd else myFixture.editor.caretModel.offset
        return descriptor.getElementsToSurround(myFixture.file, start, end)
    }

    fun `test surrounds a single statement at the caret with if`() {
        val result = surround("<caret>x = 1;\n", VRLIfSurrounder())
        assertEquals("if true {\n    x = 1;\n}\n", result)
    }

    fun `test surrounds a selection of multiple statements with if`() {
        val result = surround("<selection>x = 1;\ny = 2;\n</selection>", VRLIfSurrounder())
        assertEquals("if true {\n    x = 1;\n    y = 2;\n}\n", result)
    }

    fun `test surrounds with if-else`() {
        val result = surround("<caret>x = 1;\n", VRLIfElseSurrounder())
        assertEquals("if true {\n    x = 1;\n} else {\n\n}\n", result)
    }

    fun `test surrounds a statement inside a block`() {
        val result = surround("if true {\n    <caret>x = 1;\n}\n", VRLIfSurrounder())
        assertEquals("if true {\n    if true {\n        x = 1;\n    }\n}\n", result)
    }

    fun `test descriptor exposes both surrounders`() {
        val surrounders = VRLStatementsSurroundDescriptor().surrounders
        assertEquals(setOf("if", "if / else"), surrounders.map { it.templateDescription }.toSet())
    }

    fun `test descriptor is not exclusive`() {
        assertFalse(VRLStatementsSurroundDescriptor().isExclusive)
    }

    fun `test getElementsToSurround returns nothing at an empty file`() {
        myFixture.configureByText("t.vrl", "")
        val descriptor = VRLStatementsSurroundDescriptor()
        assertTrue(descriptor.getElementsToSurround(myFixture.file, 0, 0).isEmpty())
    }
}
