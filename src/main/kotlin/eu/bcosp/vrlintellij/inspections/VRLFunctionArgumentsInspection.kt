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

/**
 * Validates a call's arguments against the metadata in [allFunctions]: unknown named arguments,
 * arguments named more than once (or named after already being filled positionally), more
 * positional arguments than the function declares, and missing required arguments. This mirrors
 * checks the real VRL compiler performs, the same spirit as [VRLUnhandledFallibleCallInspection].
 */
class VRLFunctionArgumentsInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType != VRLElementTypes.FUNCTION_CALL) return
                val function = allFunctions[element.text] ?: return
                val arguments = callArguments(element) ?: return
                checkArguments(element, function, arguments, holder)
            }
        }
    }

    private fun callArguments(functionCall: PsiElement): List<VRLArgument>? {
        val primaryExpr = functionCall.parent as? VRLPrimaryExpr ?: return null
        val postfixExpr = primaryExpr.parent as? VRLPostfixExpr ?: return null
        val callSuffix = postfixExpr.postfixSuffixList.firstOrNull()?.callSuffix ?: return null
        return callSuffix.argumentList?.argumentList ?: emptyList()
    }

    private fun checkArguments(
        functionCall: PsiElement,
        function: VRLFunction,
        arguments: List<VRLArgument>,
        holder: ProblemsHolder,
    ) {
        val declaredNames = function.arguments.map { it.name }.toSet()
        val positional = arguments.filter { !it.isNamed() }
        val named = arguments.filter { it.isNamed() }

        val filledByPosition = positional.take(function.arguments.size).indices.map { function.arguments[it].name }.toSet()
        positional.drop(function.arguments.size).forEach { extra ->
            holder.registerProblem(
                extra,
                "Function '${function.name}' takes at most ${function.arguments.size} argument(s)",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            )
        }

        val seenNames = mutableSetOf<String>()
        for (argument in named) {
            val identifier = argument.identifier ?: continue
            val argName = identifier.text
            when {
                argName !in declaredNames ->
                    holder.registerProblem(
                        identifier,
                        "Function '${function.name}' has no argument named '$argName'",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    )

                !seenNames.add(argName) ->
                    holder.registerProblem(
                        identifier,
                        "Argument '$argName' is specified more than once",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    )

                argName in filledByPosition ->
                    holder.registerProblem(
                        identifier,
                        "Argument '$argName' is already given positionally",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    )
            }
        }

        val satisfied = filledByPosition + seenNames
        val missing = function.arguments.filter { it.isRequired && it.name !in satisfied }.map { it.name }
        if (missing.isNotEmpty()) {
            holder.registerProblem(
                functionCall,
                "Missing required argument(s): ${missing.joinToString(", ") { "'$it'" }}",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            )
        }
    }

    // A named argument is `IDENTIFIER COLON expression`; a bare identifier used positionally
    // (`assignment_expr`) also has a non-null `identifier`, so the COLON must be checked too.
    private fun VRLArgument.isNamed(): Boolean =
        identifier != null && node.findChildByType(VRLElementTypes.COLON) != null
}
