package eu.bcosp.vrlintellij.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import eu.bcosp.vrlintellij.psi.VRLBlockExpr

/**
 * VRL's own reference states a block ("`{ expressions }`") "cannot be empty", but
 * `block_expr ::= LBRACE statement* RBRACE` accepts zero statements at parse time - enforcing
 * "at least one statement" is a semantic rule, not a syntactic one, the same way
 * [VRLUnhandledFallibleCallInspection] enforces another of VRL's compile-time-only rules. A block
 * can appear as an if/else body, a closure body, or standalone as an expression value (it's one
 * of `primary_expr`'s alternatives) - all three positions are covered by matching on
 * [VRLBlockExpr] directly rather than on any one of its parent constructs.
 */
class VRLEmptyBlockInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element !is VRLBlockExpr) return
                if (element.statementList.isNotEmpty()) return

                holder.registerProblem(
                    element,
                    "Block cannot be empty",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                )
            }
        }
    }
}
