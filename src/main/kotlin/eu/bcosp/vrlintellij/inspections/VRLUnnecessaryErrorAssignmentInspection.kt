package eu.bcosp.vrlintellij.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLMultiAssignmentExpr

/**
 * Flags `value, err = <expression that can't fail>`, which VRL rejects as compiler error 104
 * ("unnecessary error assignment") - the error-destructuring form is only allowed when the
 * right-hand side can actually fail. The compiler points at the `err` target itself ("this error
 * assignment is unnecessary"), so the warning goes there rather than on the whole statement.
 *
 * No quick fix: the compiler's suggestion for `v, err = parse_json!("{}")` is
 * `use: v = parse_json("{}")`, which both drops the `err` target *and* removes the `!`. Which of
 * those the author meant isn't knowable here - and dropping `err` alone would break any later
 * reference to it - so the choice is left to them.
 *
 * Which right-hand sides count as unfailable is deliberately narrow - see
 * [isStructurallyInfallible].
 */
class VRLUnnecessaryErrorAssignmentInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType != VRLElementTypes.MULTI_ASSIGNMENT_EXPR) return
                val multiAssignment = element as VRLMultiAssignmentExpr

                val value = multiAssignment.assignmentExpr ?: return
                if (!isStructurallyInfallible(value.node)) return

                val errorTarget = multiAssignment.orExprList.getOrNull(1) ?: return
                holder.registerProblem(
                    errorTarget,
                    "Unnecessary error assignment: the assigned expression can't fail, so this " +
                        "error target is always null (compiler error 104)",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                )
            }
        }
    }
}
