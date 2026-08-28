package eu.bcosp.vrlintellij.postfix

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLPostfixTemplateTest : BasePlatformTestCase() {

    private fun configure(text: String): Int {
        myFixture.configureByText("t.vrl", text)
        return myFixture.caretOffset
    }

    private fun contextAt(offset: Int) = myFixture.file.findElementAt(offset - 1)!!

    private fun expand(template: PostfixTemplate, context: PsiElement) {
        WriteCommandAction.runWriteCommandAction(project) { template.expand(context, myFixture.editor) }
    }

    fun testIfErrIsApplicableAfterAFallibleCall() {
        val offset = configure("parse_json(.message)<caret>\n")
        val template = VRLIfErrPostfixTemplate(VRLPostfixTemplateProvider())
        assertTrue(template.isApplicable(contextAt(offset), myFixture.editor.document, offset))
    }

    fun testIfErrIsNotApplicableAfterAnInfallibleCall() {
        val offset = configure("upcase(\"a\")<caret>\n")
        val template = VRLIfErrPostfixTemplate(VRLPostfixTemplateProvider())
        assertFalse(template.isApplicable(contextAt(offset), myFixture.editor.document, offset))
    }

    fun testIfErrIsNotApplicableMidExpression() {
        val offset = configure("parse_json(.message)<caret> + 1\n")
        val template = VRLIfErrPostfixTemplate(VRLPostfixTemplateProvider())
        assertFalse(template.isApplicable(contextAt(offset), myFixture.editor.document, offset))
    }

    fun testIfErrExpandsBareStatementUsingUnderscoreAsTarget() {
        val offset = configure("parse_json(.message)<caret>\n")
        expand(VRLIfErrPostfixTemplate(VRLPostfixTemplateProvider()), contextAt(offset))
        val text = myFixture.file.text
        assertTrue(text.startsWith("_, err = parse_json(.message)\nif err != null {\n"))
        assertTrue(text.trimEnd('\n').endsWith("}"))
    }

    fun testIfErrExpandsAssignedStatementKeepingItsTarget() {
        val offset = configure("x = parse_json(.message)<caret>\n")
        expand(VRLIfErrPostfixTemplate(VRLPostfixTemplateProvider()), contextAt(offset))
        assertTrue(myFixture.file.text.startsWith("x, err = parse_json(.message)\nif err != null {\n"))
    }

    fun testIfErrPlacesCaretOnTheBlankLineInsideTheIfBlock() {
        val offset = configure("parse_json(.message)<caret>\n")
        expand(VRLIfErrPostfixTemplate(VRLPostfixTemplateProvider()), contextAt(offset))
        val caret = myFixture.editor.caretModel.offset
        val text = myFixture.editor.document.text
        val lineNumber = text.substring(0, caret).count { it == '\n' }
        assertEquals(2, lineNumber)
        val lineStart = text.lastIndexOf('\n', caret - 1) + 1
        val lineEnd = text.indexOf('\n', caret)
        assertEquals("", text.substring(lineStart, lineEnd).trim())
    }

    fun testRaiseIsApplicableAfterAFallibleCall() {
        val offset = configure("parse_json(.message)<caret>\n")
        val template = VRLRaisePostfixTemplate(VRLPostfixTemplateProvider())
        assertTrue(template.isApplicable(contextAt(offset), myFixture.editor.document, offset))
    }

    fun testRaiseIsNotApplicableWhenAlreadyRaised() {
        val offset = configure("parse_json!(.message)<caret>\n")
        val template = VRLRaisePostfixTemplate(VRLPostfixTemplateProvider())
        assertFalse(template.isApplicable(contextAt(offset), myFixture.editor.document, offset))
    }

    fun testRaiseInsertsBangAfterTheFunctionName() {
        val offset = configure("x = parse_json(.message)<caret>\n")
        expand(VRLRaisePostfixTemplate(VRLPostfixTemplateProvider()), contextAt(offset))
        assertEquals("x = parse_json!(.message)\n", myFixture.file.text)
    }
}
