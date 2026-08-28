package eu.bcosp.vrlintellij.postfix

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement

/**
 * `fallible_call(...).raise` expands to `fallible_call!(...)`, inserting VRL's raise-on-error
 * suffix right after the function name (mirroring
 * [eu.bcosp.vrlintellij.inspections.VRLUnhandledFallibleCallInspection]'s own quick fix, reachable
 * via postfix-typing instead).
 */
class VRLRaisePostfixTemplate(provider: PostfixTemplateProvider) :
    PostfixTemplate(null, "raise", ".raise", "call!(...)", provider) {

    override fun isApplicable(context: PsiElement, copyDocument: Document, newOffset: Int): Boolean {
        val (_, valueExpr) = postfixTarget(context, newOffset) ?: return false
        return isFallibleCall(valueExpr)
    }

    override fun expand(context: PsiElement, editor: Editor) {
        val (_, valueExpr) = postfixTarget(context, editor.caretModel.offset) ?: return
        val call = plainCallToken(valueExpr) ?: return
        val insertOffset = call.textRange.endOffset
        editor.document.insertString(insertOffset, "!")
        PsiDocumentManager.getInstance(context.project).commitDocument(editor.document)
        editor.caretModel.moveToOffset(insertOffset + 1)
    }
}
