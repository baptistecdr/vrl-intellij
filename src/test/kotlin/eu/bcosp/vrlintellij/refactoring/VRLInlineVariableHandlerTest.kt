package eu.bcosp.vrlintellij.refactoring

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.util.CommonRefactoringUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import eu.bcosp.vrlintellij.psi.VRLPrimaryExpr

class VRLInlineVariableHandlerTest : BasePlatformTestCase() {

    // Mirrors what the real "Inline" action does before calling canInlineElement/inlineElement:
    // resolve a usage's reference to its declaration, or use the element as-is if it's already
    // the declaration (a bare assignment target has no reference of its own - see
    // VRLPrimaryExprMixin.getReferences()).
    private fun targetAt(needle: String): PsiElement {
        val offset = myFixture.file.text.indexOf(needle)
        val element = myFixture.file.findElementAt(offset)!!
        val primaryExpr = PsiTreeUtil.getParentOfType(element, VRLPrimaryExpr::class.java, false)!!
        return primaryExpr.reference?.resolve() ?: primaryExpr
    }

    private fun inline(text: String, needle: String) {
        myFixture.configureByText("t.vrl", text)
        VRLInlineVariableHandler().inlineElement(project, myFixture.editor, targetAt(needle))
    }

    private fun errorMessage(text: String, needle: String): String? {
        myFixture.configureByText("t.vrl", text)
        return try {
            VRLInlineVariableHandler().inlineElement(project, myFixture.editor, targetAt(needle))
            null
        } catch (e: CommonRefactoringUtil.RefactoringErrorHintException) {
            e.message
        }
    }

    fun testInlinesASimpleUsage() {
        inline("x = 1\ny = x\n", "x")
        assertEquals("y = 1\n", myFixture.file.text)
    }

    fun testInlinesEveryUsage() {
        inline("x = 1\ny = x\nz = x\n", "x")
        assertEquals("y = 1\nz = 1\n", myFixture.file.text)
    }

    fun testWorksWhenInvokedFromAUsageRatherThanTheDeclaration() {
        inline("x = 1\ny = x\n", "x\n") // lands on the usage, not the "x = 1" declaration
        assertEquals("y = 1\n", myFixture.file.text)
    }

    fun testWrapsACompoundRhsInParens() {
        inline("x = 1 + 2\ny = x * 3\n", "x")
        assertEquals("y = (1 + 2) * 3\n", myFixture.file.text)
    }

    fun testDoesNotWrapAnAtomicRhs() {
        inline("x = upcase(\"a\")\ny = x\n", "x")
        assertEquals("y = upcase(\"a\")\n", myFixture.file.text)
    }

    fun testAllowsAPostfixSuffixOnAnInlinedCompoundRhs() {
        // Postfix suffixes (`.a`) only ever attach to a primary_expr - without the parens this
        // wouldn't just be lower precedence than intended, it wouldn't parse at all.
        inline("x = 1 + 2\ny = x.a\n", "x")
        assertEquals("y = (1 + 2).a\n", myFixture.file.text)
    }

    fun testCanInlineElementIsFalseForMultiAssignmentTarget() {
        myFixture.configureByText("t.vrl", "value, err = parse_json(.message)\n")
        assertFalse(VRLInlineVariableHandler().canInlineElement(targetAt("value")))
    }

    fun testCanInlineElementIsFalseWhenRhsIsItselfAnAssignment() {
        myFixture.configureByText("t.vrl", "x = y = 1\n")
        assertFalse(VRLInlineVariableHandler().canInlineElement(targetAt("x")))
    }

    fun testShowsErrorHintWhenThereAreNoUsages() {
        val message = errorMessage("x = 1\n", "x")
        assertNotNull("expected an error hint when the variable has no usages", message)
    }
}
