package eu.bcosp.vrlintellij.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLIfExpr

/**
 * Flags an `if` whose condition is a literal that isn't a boolean (`if "a" { }`), which VRL
 * rejects as compiler error 102 ("non-boolean predicate": *this predicate must resolve to a
 * boolean / instead it resolves to string*).
 *
 * Restricted to literals for the same reason [VRLArgumentTypeMismatchInspection] is: only a
 * literal has a type that's knowable without inference. That isn't just caution about calls and
 * operators - VRL narrows path and variable types through assignment, so `if .foo { }` is an
 * error on its own but perfectly valid after `.foo = true`, and `if b { }` depends entirely on
 * what `b` was last assigned. Flagging those on sight would warn about code that builds.
 *
 * No quick fix: the compiler's hint is to "coerce the value to the required type using a coercion
 * function", and which coercion (or which rewritten comparison) the author wants isn't knowable.
 */
class VRLNonBooleanPredicateInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType != VRLElementTypes.IF_EXPR) return
                val condition = (element as VRLIfExpr).expression ?: return

                val type = literalTypeName(condition.node) ?: return
                if (type == "boolean") return

                holder.registerProblem(
                    condition,
                    "Non-boolean predicate: an 'if' condition must resolve to a boolean, but this " +
                        "resolves to $type (compiler error 102)",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                )
            }
        }
    }
}
