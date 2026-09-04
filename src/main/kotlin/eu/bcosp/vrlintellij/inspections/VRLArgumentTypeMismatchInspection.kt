package eu.bcosp.vrlintellij.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.ASTNode
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
 * Flags a call argument whose value is an unambiguous literal (a string, number, boolean, null,
 * regex, timestamp, array, or object literal) of a type the target parameter doesn't accept, per
 * the per-argument `types` already recorded in [allFunctions] - e.g. `split(1, ",")`, where
 * `split`'s first parameter only accepts `string`.
 *
 * Deliberately conservative: only literals get a static type at all (see [literalType]) - a
 * variable, path, function call, or any other expression could be any type at runtime, so those
 * are left unchecked rather than guessed at, avoiding false positives entirely at the cost of
 * missing every non-literal mismatch. Argument-to-parameter resolution (matching each call
 * argument to its declared parameter by name or position) mirrors
 * [VRLFunctionArgumentsInspection]'s own logic; kept as a separate inspection since it checks a
 * different, independent property of each argument.
 */
class VRLArgumentTypeMismatchInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType != VRLElementTypes.FUNCTION_CALL) return
                val function = allFunctions[element.text] ?: return
                val arguments = callArguments(element) ?: return
                checkArgumentTypes(function, arguments, holder)
            }
        }
    }

    private fun callArguments(functionCall: PsiElement): List<VRLArgument>? {
        val primaryExpr = functionCall.parent as? VRLPrimaryExpr ?: return null
        val postfixExpr = primaryExpr.parent as? VRLPostfixExpr ?: return null
        val callSuffix = postfixExpr.postfixSuffixList.firstOrNull()?.callSuffix ?: return null
        return callSuffix.argumentList?.argumentList ?: emptyList()
    }

    private fun checkArgumentTypes(function: VRLFunction, arguments: List<VRLArgument>, holder: ProblemsHolder) {
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

            val valueNode = (argument.expression ?: argument.assignmentExpr)?.node ?: continue
            val actualType = literalType(valueNode) ?: continue
            if ("any" in declared.types || actualType in declared.types) continue

            holder.registerProblem(
                valueNode.psi,
                "Argument '${declared.name}' of '${function.name}' expects " +
                    "${declared.types.sorted().joinToString(" | ")} but got $actualType",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            )
        }
    }

    private fun literalType(node: ASTNode): String? = when (collapsePassThroughWrappers(node).elementType) {
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
}
