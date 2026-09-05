package eu.bcosp.vrlintellij.todo

import com.intellij.psi.search.PsiTodoSearchHelper
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLIndexPatternBuilderTest : BasePlatformTestCase() {

    fun `test TODO comments are found by the TODO search helper`() {
        myFixture.configureByText("t.vrl", "# TODO: fix this\nx = 1;\n")
        val items = PsiTodoSearchHelper.getInstance(project).findTodoItems(myFixture.file)
        assertEquals(1, items.size)
    }

    fun `test a plain comment with no TODO marker is not flagged`() {
        myFixture.configureByText("t.vrl", "# just a comment\nx = 1;\n")
        val items = PsiTodoSearchHelper.getInstance(project).findTodoItems(myFixture.file)
        assertEquals(0, items.size)
    }

    fun `test text outside a comment is not flagged even if it contains TODO`() {
        myFixture.configureByText("t.vrl", "x = \"TODO: not a real todo\";\n")
        val items = PsiTodoSearchHelper.getInstance(project).findTodoItems(myFixture.file)
        assertEquals(0, items.size)
    }
}
