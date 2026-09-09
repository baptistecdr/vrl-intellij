package eu.bcosp.vrlintellij.intentions

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import eu.bcosp.vrlintellij.psi.VRLAssignmentExpr
import eu.bcosp.vrlintellij.psi.VRLCallSuffix
import eu.bcosp.vrlintellij.psi.VRLMultiAssignmentExpr
import eu.bcosp.vrlintellij.psi.VRLPostfixExpr
import eu.bcosp.vrlintellij.psi.collapsePassThroughWrappers
import eu.bcosp.vrlintellij.references.VRLVariableResolver

/**
 * Helpers shared by the two intentions that move between VRL's error-handling forms - raising with
 * `!` and destructuring with `value, err = ...`.
 *
 * Both shapes were checked against `vector vrl` 0.58.0, including for path targets:
 * `.a = parse_json!("{}")` and `.a, err = parse_json("{}")` are both accepted, so neither
 * intention needs to be limited to variable targets.
 */
internal object VRLErrorHandlingForms {

    /** The call suffix of `target = f(...)`, when the right-hand side is exactly one call. */
    fun assignedCall(assignment: VRLAssignmentExpr): VRLCallSuffix? = callSuffixOf(assignment.assignmentExpr)

    /** The call suffix of `value, err = f(...)`, when the right-hand side is exactly one call. */
    fun assignedCall(assignment: VRLMultiAssignmentExpr): VRLCallSuffix? = callSuffixOf(assignment.assignmentExpr)

    /**
     * Only the *outermost* suffix counts as raising: `vector vrl` accepts
     * `v, err = parse_json(string!(.a))`, because raising inside an argument doesn't stop
     * `parse_json` itself from failing.
     */
    private fun callSuffixOf(expression: PsiElement?): VRLCallSuffix? {
        val node = expression?.node ?: return null
        val postfixExpr = collapsePassThroughWrappers(node).psi as? VRLPostfixExpr ?: return null
        val suffixes = postfixExpr.postfixSuffixList
        if (suffixes.size != 1) return null
        return suffixes.firstOrNull()?.callSuffix
    }

    /** A name for the error target that nothing already in scope answers to. */
    fun freeErrorName(context: PsiElement): String {
        val taken = VRLVariableResolver.visibleVariableNames(context.textRange.endOffset, context).toSet()
        if ("err" !in taken) return "err"
        var suffix = 1
        while ("err$suffix" in taken) suffix++
        return "err$suffix"
    }

    /**
     * The nearest enclosing assignment that actually assigns something.
     *
     * The search has to keep walking rather than stop at the first `VRLAssignmentExpr`: the rule
     * is right-recursive (`assignment_expr ::= or_expr ((ASSIGN | MERGE_ASSIGN) assignment_expr)?`),
     * so in `x = parse_json!("{}")` the right-hand side is *itself* an `ASSIGNMENT_EXPR` with no
     * `=` of its own, and a caret inside the call lands on that one first.
     */
    fun enclosingAssignment(element: PsiElement): VRLAssignmentExpr? {
        var candidate = PsiTreeUtil.getParentOfType(element, VRLAssignmentExpr::class.java, false)
        while (candidate != null) {
            if (candidate.assignmentExpr != null) return candidate
            candidate = PsiTreeUtil.getParentOfType(candidate, VRLAssignmentExpr::class.java, true)
        }
        return null
    }

    fun enclosingMultiAssignment(element: PsiElement): VRLMultiAssignmentExpr? =
        PsiTreeUtil.getParentOfType(element, VRLMultiAssignmentExpr::class.java, false)

    /**
     * The `PRIMARY_EXPR` an assignment target boils down to, which is also the element
     * [VRLVariableResolver.readDeclarations] keys its set on.
     *
     * Deliberately not [collapsePassThroughWrappers]: `PRIMARY_EXPR` wraps a single identifier
     * token, so collapsing walks straight past it onto the leaf and every `as? VRLPrimaryExpr`
     * cast comes back null.
     */
    fun targetPrimaryExpr(target: PsiElement): eu.bcosp.vrlintellij.psi.VRLPrimaryExpr? =
        PsiTreeUtil.findChildOfType(target, eu.bcosp.vrlintellij.psi.VRLPrimaryExpr::class.java, false)
}
