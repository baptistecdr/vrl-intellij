package eu.bcosp.vrlintellij.intentions

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import eu.bcosp.vrlintellij.psi.VRLComparisonExpr
import eu.bcosp.vrlintellij.psi.VRLElementTypes

/**
 * A comparison at the caret, already split into its operator and the two operands.
 *
 * VRL's grammar is `comparison_expr ::= additive_expr ((EQ | NE | GT | GE | LT | LE) additive_expr)?`
 * (VRL.bnf), so the rule also matches a lone operand - hence [find] insisting on an operator token
 * before reporting a comparison at all.
 */
internal class VRLComparison(
    val expression: VRLComparisonExpr,
    val operator: ASTNode,
    val left: PsiElement,
    val right: PsiElement,
) {
    val operatorText: String get() = operator.text

    companion object {
        private val OPERATORS = setOf(
            VRLElementTypes.EQ,
            VRLElementTypes.NE,
            VRLElementTypes.GT,
            VRLElementTypes.GE,
            VRLElementTypes.LT,
            VRLElementTypes.LE,
        )

        /** The innermost comparison enclosing [element] that actually has an operator, or null. */
        fun find(element: PsiElement): VRLComparison? {
            var candidate = PsiTreeUtil.getParentOfType(element, VRLComparisonExpr::class.java, false)
            while (candidate != null) {
                val operator = candidate.node.getChildren(null).firstOrNull { it.elementType in OPERATORS }
                val operands = candidate.additiveExprList
                if (operator != null && operands.size == 2) {
                    return VRLComparison(candidate, operator, operands[0], operands[1])
                }
                candidate = PsiTreeUtil.getParentOfType(candidate, VRLComparisonExpr::class.java, true)
            }
            return null
        }
    }
}

/**
 * The operator meaning "not this comparison": `a > b` negated is `a <= b`.
 *
 * Sound for every type VRL compares - it has no NaN literal, and `vector vrl` evaluates
 * comparisons on both numbers and strings (`"a" > "b"` returns false rather than erroring), so
 * every comparison is total and the complement is exact.
 */
internal fun negatedOperator(operator: String): String? = when (operator) {
    "==" -> "!="
    "!=" -> "=="
    ">" -> "<="
    ">=" -> "<"
    "<" -> ">="
    "<=" -> ">"
    else -> null
}

/** The operator that preserves meaning once the operands swap sides: `a > b` is `b < a`. */
internal fun flippedOperator(operator: String): String? = when (operator) {
    "==" -> "=="
    "!=" -> "!="
    ">" -> "<"
    ">=" -> "<="
    "<" -> ">"
    "<=" -> ">="
    else -> null
}
