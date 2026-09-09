package eu.bcosp.vrlintellij.inspections

import com.intellij.lang.ASTNode
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLPostfixExpr
import eu.bcosp.vrlintellij.psi.collapsePassThroughWrappers

/**
 * True when an expression provably never fails, decided *without* consulting the per-function
 * `isFallible` flags in [eu.bcosp.vrlintellij.functions.allFunctions].
 *
 * VRL rejects both `<infallible> ?? x` (compiler error 651) and `v, err = <infallible>` (104), so
 * flagging either needs an "can this fail?" answer. Taking that answer from the function metadata
 * would inherit its known drift: that metadata comes from vector.dev's fallible/infallible badge,
 * which disagrees with `vector vrl` on 27 stdlib functions (`join` is documented infallible but
 * the compiler demands error handling; `chunks` is documented fallible but compiles unhandled).
 * A wrong answer here means warning about code that builds fine, so only these three shapes
 * count - each infallible by construction, whatever the callee is and whatever the docs claim:
 *
 * - a literal, which has no error case at all;
 * - a call whose *outermost* suffix carries the `!` raise flag, which turns the error into an
 *   abort. Outermost is the load-bearing part: `vector vrl` accepts
 *   `v, err = parse_json(string!(.a))`, because raising inside an argument doesn't stop
 *   `parse_json` itself from failing;
 * - a `??` coalescing expression, which has already consumed the error.
 *
 * Everything else - a bare call, a path, a variable, arithmetic - is deliberately left alone.
 * That does miss real errors (`vector vrl` also rejects `upcase("a") ?? "x"` and `v, err = .foo`),
 * but a missed warning costs the user nothing, while a wrong one costs them trust in the rest.
 */
internal fun isStructurallyInfallible(node: ASTNode): Boolean {
    val expression = collapsePassThroughWrappers(node)
    return literalTypeName(expression) != null || isRaisedCall(expression) || isCoalescing(expression)
}

/**
 * The VRL type name of a bare literal (`"a"` -> `string`, `1` -> `integer`, `[1]` -> `array`, ...),
 * or null for anything whose type isn't statically knowable - a variable, path, call, or operator
 * expression could be any type at runtime.
 *
 * Only reached through [collapsePassThroughWrappers], so a literal buried under the precedence
 * chain still resolves; a parenthesized one (`(1)`) deliberately doesn't, since the parentheses
 * leave more than one significant child for the collapse to stop on.
 */
internal fun literalTypeName(node: ASTNode): String? = when (collapsePassThroughWrappers(node).elementType) {
    VRLElementTypes.STRING, VRLElementTypes.RAW_STRING -> "string"
    VRLElementTypes.INTEGER_LITERAL -> "integer"
    VRLElementTypes.FLOAT_LITERAL -> "float"
    VRLElementTypes.TRUE, VRLElementTypes.FALSE -> "boolean"
    VRLElementTypes.NULL -> "null"
    VRLElementTypes.REGEX -> "regex"
    VRLElementTypes.TIMESTAMP -> "timestamp"
    VRLElementTypes.ARRAY_EXPR -> "array"
    VRLElementTypes.OBJECT_EXPR -> "object"
    else -> null
}

private fun isRaisedCall(node: ASTNode): Boolean {
    val postfixExpr = node.psi as? VRLPostfixExpr ?: return false
    return postfixExpr.postfixSuffixList.firstOrNull()?.callSuffix?.raiseFlag != null
}

// The `??` rule is `comparison_expr (NULL_COALESCE comparison_expr)*`, so the node also exists
// with no operator at all (and is then collapsed away) - the token has to be there for real.
internal fun isCoalescing(node: ASTNode): Boolean =
    node.elementType == VRLElementTypes.NULL_COALESCE_EXPR &&
        node.getChildren(null).any { it.elementType == VRLElementTypes.NULL_COALESCE }
