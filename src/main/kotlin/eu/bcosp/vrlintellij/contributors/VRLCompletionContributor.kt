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
import eu.bcosp.vrlintellij.functions.VRLFunctionArgument
import eu.bcosp.vrlintellij.functions.allFunctions
import eu.bcosp.vrlintellij.psi.VRLArgument
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

        // A string literal is otherwise excluded from completion entirely (see NON_COMPLETABLE
        // below) - this is the one exception, and only when it's the value of an argument with a
        // known fixed set of accepted strings (e.g. encode_base64's charset: "standard").
        // Anything else about a string's contents (a path, a variable name, ...) isn't something
        // this plugin has a way to suggest.
        if (position.node?.elementType == VRLElementTypes.STRING) {
            completeEnumValue(position, parameters.offset, result)
            return
        }
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

    private fun completeEnumValue(position: PsiElement, offset: Int, result: CompletionResultSet) {
        val call = enclosingCall(position) ?: return
        val argument = PsiTreeUtil.getParentOfType(position, VRLArgument::class.java) ?: return
        val declared = declaredArgumentFor(call, argument) ?: return
        if (declared.enumValues.isEmpty()) return

        // The prefix is whatever the user already typed between the opening quote and the caret -
        // position.containingFile is the completion copy with the dummy identifier inserted right
        // at the caret, so this substring stops short of it rather than including it.
        val stringStart = position.textRange.startOffset
        val prefixEnd = offset.coerceIn(stringStart + 1, position.textRange.endOffset)
        val prefix = position.containingFile.text.substring(stringStart + 1, prefixEnd)

        // Filtered explicitly rather than relying solely on the prefix matcher below to narrow
        // what's shown - that matcher still governs correctly replacing the typed prefix on
        // accept (and highlighting the matched portion), but isn't a hard filter on its own.
        val withPrefix = result.withPrefixMatcher(prefix)
        declared.enumValues
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .forEach { value -> withPrefix.addElement(LookupElementBuilder.create(value).withIcon(AllIcons.Nodes.Enum)) }
    }

    private fun declaredArgumentFor(call: VRLCallSuffix, argument: VRLArgument): VRLFunctionArgument? {
        val postfixExpr = PsiTreeUtil.getParentOfType(call, VRLPostfixExpr::class.java) ?: return null
        val functionName = postfixExpr.primaryExpr.functionCall?.text ?: return null
        val function = allFunctions[functionName] ?: return null

        if (argument.isNamed()) {
            return function.arguments.find { it.name == argument.identifier?.text }
        }

        val positional = call.argumentList?.argumentList?.filter { !it.isNamed() } ?: return null
        val positionalIndex = positional.indexOf(argument)
        if (positionalIndex < 0) return null
        return function.arguments.getOrNull(positionalIndex)
    }

    // A named argument is `IDENTIFIER COLON expression`; a bare identifier used positionally
    // (`assignment_expr`) also has a non-null `identifier`, so the COLON must be checked too.
    private fun VRLArgument.isNamed(): Boolean =
        identifier != null && node.findChildByType(VRLElementTypes.COLON) != null

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
