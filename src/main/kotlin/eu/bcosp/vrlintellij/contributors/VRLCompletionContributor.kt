package eu.bcosp.vrlintellij.contributors

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.TextRange
import eu.bcosp.vrlintellij.VRL
import eu.bcosp.vrlintellij.functions.VRLFunction
import eu.bcosp.vrlintellij.functions.allFunctions


class VRLCompletionContributor : CompletionContributor() {

    override fun fillCompletionVariants(
        parameters: CompletionParameters,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        if (position.language != VRL) return
        allFunctions
            .map { (name, function) ->
                val lookupElement = createEnhancedLookupElement(name, function)
                val frequencyScore = 100
                PrioritizedLookupElement.withPriority(lookupElement, frequencyScore.toDouble())
            }
            .sortedByDescending { 100 }
            .forEach { result.addElement(it) }
    }

    private fun createEnhancedLookupElement(
        functionName: String,
        function: VRLFunction
    ): LookupElement {
        val argumentsStr = buildArgumentsString(function)
        val returnTypeStr = function.returnTypes.joinToString("|")

        return LookupElementBuilder
            .create(functionName)
            .withIcon(AllIcons.Nodes.Function)
            .withTypeText(returnTypeStr, true)
            .withTailText("($argumentsStr)", true)
            .bold()
            .withInsertHandler { context, _ ->
                insertFunctionWithParameters(context)
            }
    }

    private fun buildArgumentsString(function: VRLFunction): String {
        return function.arguments
            .joinToString(", ") { arg ->
                val required = if (arg.isRequired) "" else "?"
                "${arg.name}: ${arg.types.joinToString("|")}$required"
            }
            .take(60) + if (function.arguments.size > 3) "..." else ""
    }

    private fun insertFunctionWithParameters(context: InsertionContext) {
        val text = context.document.getText(TextRange(context.startOffset, context.selectionEndOffset))

        if (text.endsWith("(")) {
            return
        }
        context.document.insertString(context.selectionEndOffset, "()")
        context.editor.caretModel.moveToOffset(context.selectionEndOffset - 1)
    }
}