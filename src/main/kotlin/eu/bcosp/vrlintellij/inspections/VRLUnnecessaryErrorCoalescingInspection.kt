package eu.bcosp.vrlintellij.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLNullCoalesceExpr

/**
 * Flags `<expression that can't fail> ?? <fallback>`, which VRL rejects outright as compiler
 * error 651 ("unnecessary error coalescing operation") - the fallback is unreachable, so the code
 * doesn't build. The compiler's own annotation labels the left operand "this expression can't
 * fail", the operator "remove this error coalescing operation", and the fallback "this expression
 * never resolves", which is why the warning lands on the left operand and the fix drops the rest.
 *
 * Only the left operand matters, including in a chain: `vector vrl` accepts
 * `parse_json("{}") ?? parse_json("{}") ?? {}` because the first operand really can fail.
 *
 * Which expressions count as unfailable is deliberately narrow - see [isStructurallyInfallible].
 */
class VRLUnnecessaryErrorCoalescingInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                val node = element.node ?: return
                if (node.elementType != VRLElementTypes.NULL_COALESCE_EXPR) return
                if (!isCoalescing(node)) return

                val left = (element as VRLNullCoalesceExpr).comparisonExprList.firstOrNull() ?: return
                if (!isStructurallyInfallible(left.node)) return

                holder.registerProblem(
                    left,
                    "Unnecessary error coalescing: this expression can't fail, so the '??' " +
                        "fallback never resolves (compiler error 651)",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    RemoveCoalescingQuickFix,
                )
            }
        }
    }

    private object RemoveCoalescingQuickFix : LocalQuickFix {
        override fun getFamilyName(): String = "Remove '??' fallback"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val left = descriptor.psiElement ?: return
            val coalesce = left.parent ?: return
            val document = PsiDocumentManager.getInstance(project).getDocument(coalesce.containingFile) ?: return
            document.replaceString(coalesce.textRange.startOffset, coalesce.textRange.endOffset, left.text)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }
}
