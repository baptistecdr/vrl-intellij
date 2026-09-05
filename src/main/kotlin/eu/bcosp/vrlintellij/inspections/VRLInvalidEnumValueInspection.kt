package eu.bcosp.vrlintellij.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import eu.bcosp.vrlintellij.functions.VRLFunction
import eu.bcosp.vrlintellij.functions.allFunctions
import eu.bcosp.vrlintellij.psi.VRLArgument
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLPostfixExpr
import eu.bcosp.vrlintellij.psi.VRLPrimaryExpr
import eu.bcosp.vrlintellij.psi.collapsePassThroughWrappers

/**
 * Flags a call argument whose value is a plain string literal that doesn't match any of the
 * argument's known enum values - e.g. `encode_base64(x, charset: "bogus")`, where `charset` only
 * accepts `"standard"` or `"url_safe"`. Values come from vrl's own stdlib source (see
 * [eu.bcosp.vrlintellij.functions.VRLFunctionArgument.enumValues] and
 * scripts/refresh-vrl-functions.mjs), not vector.dev's docs, which rarely spell them out - real
 * ground truth the plugin's own type system has no way to derive on its own.
 *
 * Deliberately only STRING literals (never a raw string, string interpolation, a variable, or any
 * other expression) - those can't be reliably read back as a plain compile-time value here, so
 * checking only the unambiguous case avoids false positives entirely. Argument-to-parameter
 * resolution (matching each call argument to its declared parameter by name or position) mirrors
 * [eu.bcosp.vrlintellij.inspections.VRLArgumentTypeMismatchInspection]'s own logic; kept as a
 * separate inspection since it checks a different, independent property of each argument.
 */
class VRLInvalidEnumValueInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType != VRLElementTypes.FUNCTION_CALL) return
                val function = allFunctions[element.text] ?: return
                val arguments = callArguments(element) ?: return
                checkEnumValues(function, arguments, holder)
            }
        }
    }

    private fun callArguments(functionCall: PsiElement): List<VRLArgument>? {
        val primaryExpr = functionCall.parent as? VRLPrimaryExpr ?: return null
        val postfixExpr = primaryExpr.parent as? VRLPostfixExpr ?: return null
        val callSuffix = postfixExpr.postfixSuffixList.firstOrNull()?.callSuffix ?: return null
        return callSuffix.argumentList?.argumentList ?: emptyList()
    }

    private fun checkEnumValues(function: VRLFunction, arguments: List<VRLArgument>, holder: ProblemsHolder) {
        val declaredByName = function.arguments.associateBy { it.name }
        var positionalIndex = 0
        for (argument in arguments) {
            val identifier = argument.identifier
            val isNamed = identifier != null && argument.node.findChildByType(VRLElementTypes.COLON) != null
            val declared = if (isNamed) {
                declaredByName[identifier.text]
            } else {
                function.arguments.getOrNull(positionalIndex).also { positionalIndex++ }
            } ?: continue
            if (declared.enumValues.isEmpty()) continue

            val valueNode = (argument.expression ?: argument.assignmentExpr)?.node ?: continue
            val literalNode = collapsePassThroughWrappers(valueNode)
            if (literalNode.elementType != VRLElementTypes.STRING) continue

            val actual = stringLiteralValue(literalNode.text)
            if (actual in declared.enumValues) continue

            holder.registerProblem(
                literalNode.psi,
                "Argument '${declared.name}' of '${function.name}' expects one of: " +
                    declared.enumValues.joinToString(", ") { "'$it'" } + " but got '$actual'",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            )
        }
    }

    // Enum values are always plain identifier-like tokens (e.g. "standard", "SHA-256") that never
    // need escaping, so only the two escapes that could otherwise corrupt the comparison - an
    // escaped quote or backslash - are undone; anything else is left as-is.
    private fun stringLiteralValue(text: String): String =
        text.removeSurrounding("\"").replace("\\\"", "\"").replace("\\\\", "\\")
}
