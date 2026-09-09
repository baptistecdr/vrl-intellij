package eu.bcosp.vrlintellij.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement

/**
 * Replaces the comparison operator at the caret with its logical complement, so `.status >= 400`
 * becomes `.status < 400`. This changes what the program does, by design - it's the quick way to
 * flip a guard around after realising the condition reads backwards; the intention text always
 * names both operators so the change isn't silent.
 *
 * Contrast [VRLFlipComparisonIntention], which swaps the operands and preserves the meaning.
 */
class VRLNegateComparisonIntention : PsiElementBaseIntentionAction() {

    override fun getFamilyName(): String = "Negate comparison"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val comparison = VRLComparison.find(element) ?: return false
        val negated = negatedOperator(comparison.operatorText) ?: return false
        text = "Negate '${comparison.operatorText}' to '$negated'"
        return true
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val comparison = VRLComparison.find(element) ?: return
        val negated = negatedOperator(comparison.operatorText) ?: return

        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(comparison.expression.containingFile) ?: return
        val range = comparison.operator.textRange
        document.replaceString(range.startOffset, range.endOffset, negated)
        documentManager.commitDocument(document)
    }
}
