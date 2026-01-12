package eu.bcosp.vrlintellij.contributors

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import eu.bcosp.vrlintellij.VRL
import eu.bcosp.vrlintellij.functions.VRLFunction
import eu.bcosp.vrlintellij.functions.allFunctions
import eu.bcosp.vrlintellij.psi.VRLArgumentList
import eu.bcosp.vrlintellij.psi.VRLCallSuffix
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLPathSegment
import eu.bcosp.vrlintellij.psi.VRLPostfixExpr
import eu.bcosp.vrlintellij.psi.VRLPostfixSuffix
import eu.bcosp.vrlintellij.references.VRLVariableResolver

private val NON_COMPLETABLE = TokenSet.create(
    VRLElementTypes.COMMENT,
    VRLElementTypes.STRING,
    VRLElementTypes.RAW_STRING,
    VRLElementTypes.REGEX,
    VRLElementTypes.TIMESTAMP,
)

class VRLCompletionContributor : CompletionContributor() {

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val position = parameters.position
        if (position.language != VRL) return
        if (position.node?.elementType in NON_COMPLETABLE) return

        // Right after `.` (an event field path, or `x.field` access) there's nothing this
        // plugin knows to suggest — it has no schema for event fields, and a function or
        // variable name there wouldn't mean what it looks like it means.
        val parent = position.parent
        if (parent is VRLPathSegment || parent is VRLPostfixSuffix) return

        enclosingCall(position)?.let { call ->
            completeArgumentNames(call, result)
        }

        allFunctions.forEach { (name, function) -> result.addElement(functionLookupElement(name, function)) }
        VRLVariableResolver.visibleVariableNames(parameters.offset, position).forEach { name ->
            result.addElement(variableLookupElement(name))
        }
    }

    private fun enclosingCall(position: PsiElement): VRLCallSuffix? {
        val argumentList = PsiTreeUtil.getParentOfType(position, VRLArgumentList::class.java) ?: return null
        return argumentList.parent as? VRLCallSuffix
    }

    private fun completeArgumentNames(call: VRLCallSuffix, result: CompletionResultSet) {
        val postfixExpr = PsiTreeUtil.getParentOfType(call, VRLPostfixExpr::class.java) ?: return
        val functionName = postfixExpr.primaryExpr.functionCall?.text ?: return
        val function = allFunctions[functionName] ?: return

        val alreadyUsed = call.argumentList?.argumentList
            ?.mapNotNull { it.identifier?.text }
            ?.toSet()
            ?: emptySet()

        function.arguments
            .filter { it.name !in alreadyUsed }
            .forEach { arg ->
                val lookupElement = LookupElementBuilder
                    .create("${arg.name}: ")
                    .withPresentableText(arg.name)
                    .withIcon(AllIcons.Nodes.Parameter)
                    .withTypeText(arg.types.joinToString("|"), true)
                    .withTailText(if (arg.isRequired) "" else " (optional)", true)
                result.addElement(PrioritizedLookupElement.withPriority(lookupElement, 200.0))
            }
    }

    private fun functionLookupElement(name: String, function: VRLFunction): LookupElement {
        val argumentsStr = buildArgumentsString(function)
        val returnTypeStr = function.returnTypes.joinToString("|")

        val lookupElement = LookupElementBuilder
            .create(name)
            .withIcon(AllIcons.Nodes.Function)
            .withTypeText(returnTypeStr, true)
            .withTailText("($argumentsStr)", true)
            .bold()
            .withInsertHandler { context, _ -> insertFunctionParens(context) }
        return PrioritizedLookupElement.withPriority(lookupElement, 100.0)
    }

    private fun variableLookupElement(name: String): LookupElement {
        val lookupElement = LookupElementBuilder
            .create(name)
            .withIcon(AllIcons.Nodes.Variable)
        return PrioritizedLookupElement.withPriority(lookupElement, 150.0)
    }

    private fun buildArgumentsString(function: VRLFunction): String {
        return function.arguments
            .joinToString(", ") { arg ->
                val required = if (arg.isRequired) "" else "?"
                "${arg.name}: ${arg.types.joinToString("|")}$required"
            }
            .take(60) + if (function.arguments.size > 3) "..." else ""
    }

    private fun insertFunctionParens(context: InsertionContext) {
        val chars = context.document.charsSequence
        if (context.selectionEndOffset < chars.length && chars[context.selectionEndOffset] == '(') {
            context.editor.caretModel.moveToOffset(context.selectionEndOffset + 1)
            return
        }
        context.document.insertString(context.selectionEndOffset, "()")
        context.editor.caretModel.moveToOffset(context.selectionEndOffset - 1)
    }
}
