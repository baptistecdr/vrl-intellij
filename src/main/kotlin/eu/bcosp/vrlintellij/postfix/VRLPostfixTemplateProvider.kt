package eu.bcosp.vrlintellij.postfix

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class VRLPostfixTemplateProvider : PostfixTemplateProvider {

    override fun getTemplates(): Set<PostfixTemplate> = setOf(
        VRLIfErrPostfixTemplate(this),
        VRLRaisePostfixTemplate(this),
    )

    override fun isTerminalSymbol(currentChar: Char): Boolean = currentChar == '.'

    override fun preExpand(file: PsiFile, editor: Editor) {}

    override fun afterExpand(file: PsiFile, editor: Editor) {}

    override fun preCheck(copyFile: PsiFile, realEditor: Editor, currentOffset: Int): PsiFile = copyFile
}
