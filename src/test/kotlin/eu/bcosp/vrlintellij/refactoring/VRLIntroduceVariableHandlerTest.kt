package eu.bcosp.vrlintellij.refactoring

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.refactoring.util.CommonRefactoringUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLIntroduceVariableHandlerTest : BasePlatformTestCase() {

    private fun invokeRefactoring() {
        VRLIntroduceVariableHandler().invoke(project, myFixture.editor, myFixture.file, DataContext.EMPTY_CONTEXT)
    }

    private fun errorMessage(text: String): String? {
        myFixture.configureByText("t.vrl", text)
        return try {
            invokeRefactoring()
            null
        } catch (e: CommonRefactoringUtil.RefactoringErrorHintException) {
            e.message
        }
    }

    fun testExtractsSelectedExpressionBeforeTheStatement() {
        myFixture.configureByText("t.vrl", "x = <selection>1 + 2</selection>\n")
        invokeRefactoring()
        assertEquals("value = 1 + 2\nx = value\n", myFixture.file.text)
    }

    fun testSelectsTheNewNameForImmediateRename() {
        myFixture.configureByText("t.vrl", "x = <selection>1 + 2</selection>\n")
        invokeRefactoring()
        assertEquals("value", myFixture.editor.selectionModel.selectedText)
    }

    fun testPreservesTheEnclosingStatementsIndent() {
        myFixture.configureByText("t.vrl", "if true {\n    x = <selection>1 + 2</selection>\n}\n")
        invokeRefactoring()
        assertEquals("if true {\n    value = 1 + 2\n    x = value\n}\n", myFixture.file.text)
    }

    fun testAvoidsCollidingWithAnExistingIdentifier() {
        myFixture.configureByText("t.vrl", "value = 1;\nx = <selection>1 + 2</selection>\n")
        invokeRefactoring()
        assertEquals("value = 1;\nvalue2 = 1 + 2\nx = value2\n", myFixture.file.text)
    }

    fun testExtractsAFunctionCallArgument() {
        myFixture.configureByText("t.vrl", "x = upcase(<selection>.message</selection>)\n")
        invokeRefactoring()
        assertEquals("value = .message\nx = upcase(value)\n", myFixture.file.text)
    }

    fun testFailsWithoutASelection() {
        val message = errorMessage("x = 1 + 2\n")
        assertNotNull("expected an error hint when nothing is selected", message)
    }

    fun testFailsWhenSelectionDoesNotExactlyCoverAnExpression() {
        // Selects "1 + " - a partial, non-expression fragment.
        val message = errorMessage("x = <selection>1 + </selection>2\n")
        assertNotNull("expected an error hint for a non-exact selection", message)
    }

    fun testFailsWhenSelectionCoversTheWholeStatement() {
        val message = errorMessage("<selection>x = 1</selection>\n")
        assertNotNull("expected an error hint when the whole statement is selected", message)
    }
}
