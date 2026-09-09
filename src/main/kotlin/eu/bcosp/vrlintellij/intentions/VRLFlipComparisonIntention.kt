package eu.bcosp.vrlintellij.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement

/**
 * Swaps the operands of the comparison at the caret, mirroring the operator so the meaning is
 * unchanged: `.status > 200` becomes `200 < .status`. Useful for putting the interesting operand
 * on the left, or for lining a condition up with a neighbouring one.
 *
 * Contrast [VRLNegateComparisonIntention], which deliberately *does* change the meaning.
 */
class VRLFlipComparisonIntention : PsiElementBaseIntentionAction() {

    override fun getFamilyName(): String = "Flip comparison"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val comparison = VRLComparison.find(element) ?: return false
        val flipped = flippedOperator(comparison.operatorText) ?: return false
        text = "Flip '${comparison.operatorText}' to '$flipped'"
        return true
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val comparison = VRLComparison.find(element) ?: return
        val flipped = flippedOperator(comparison.operatorText) ?: return

        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(comparison.expression.containingFile) ?: return
        val range = comparison.expression.textRange
        document.replaceString(
            range.startOffset,
            range.endOffset,
            "${comparison.right.text} $flipped ${comparison.left.text}",
        )
        documentManager.commitDocument(document)
    }
}
