package eu.bcosp.vrlintellij.postfix

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import eu.bcosp.vrlintellij.functions.allFunctions
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLPostfixExpr
import eu.bcosp.vrlintellij.psi.VRLStatement
import eu.bcosp.vrlintellij.psi.collapsePassThroughWrappers

/**
 * Both `.iferr` and `.raise` apply to "the value expression a statement is built from" - either
 * the statement's whole (unassigned) expression, or the RHS of a plain `x = expr` assignment - and
 * only when that's exactly what the user just finished typing before the postfix key (its end
 * offset must line up with where the key was typed).
 */
internal fun postfixTarget(context: PsiElement, offset: Int): Pair<VRLStatement, PsiElement>? {
    val statement = PsiTreeUtil.getParentOfType(context, VRLStatement::class.java, false) ?: return null
    if (statement.multiAssignmentExpr != null) return null
    val assignment = statement.expression?.assignmentExpr ?: return null
    val valueExpr: PsiElement = assignment.assignmentExpr ?: statement.expression!!
    if (valueExpr.textRange.endOffset != offset) return null
    return statement to valueExpr
}

internal fun assignmentTargetText(statement: VRLStatement): String? {
    val assignment = statement.expression?.assignmentExpr ?: return null
    return if (assignment.assignmentExpr != null) assignment.orExpr.text else null
}

// The FUNCTION_CALL token of a plain, not-yet-raised `name(...)` call the given value expression
// collapses down to, or null if it isn't (a call to a value returned by something else, an
// operator expression, a literal, an already-raised `name!(...)` call, etc).
internal fun plainCallToken(valueExpr: PsiElement): PsiElement? {
    val postfixExpr = collapsePassThroughWrappers(valueExpr.node).psi as? VRLPostfixExpr ?: return null
    val callSuffix = postfixExpr.postfixSuffixList.firstOrNull()?.callSuffix ?: return null
    if (callSuffix.raiseFlag != null) return null
    return postfixExpr.primaryExpr.node.findChildByType(VRLElementTypes.FUNCTION_CALL)?.psi
}

internal fun isFallibleCall(valueExpr: PsiElement): Boolean {
    val call = plainCallToken(valueExpr) ?: return false
    return allFunctions[call.text]?.isFallible == true
}
