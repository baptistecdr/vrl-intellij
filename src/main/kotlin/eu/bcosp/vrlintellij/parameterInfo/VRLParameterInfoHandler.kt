package eu.bcosp.vrlintellij.parameterInfo

import com.intellij.lang.parameterInfo.CreateParameterInfoContext
import com.intellij.lang.parameterInfo.ParameterInfoHandler
import com.intellij.lang.parameterInfo.ParameterInfoUIContext
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import eu.bcosp.vrlintellij.functions.VRLFunction
import eu.bcosp.vrlintellij.functions.VRLFunctionArgument
import eu.bcosp.vrlintellij.functions.allFunctions
import eu.bcosp.vrlintellij.psi.VRLCallSuffix
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLPostfixExpr
import eu.bcosp.vrlintellij.psi.VRLPostfixSuffix

/**
 * Shows the signature of the function being called (Ctrl+P / View > Parameter Info). VRL has no
 * overloading - each function name has exactly one [VRLFunction] - so [getItemsToShow] always has
 * at most one element; this class exists mainly to adapt VRL's PSI to the platform's parameter-info
 * contract rather than to choose between candidates.
 */
class VRLParameterInfoHandler : ParameterInfoHandler<VRLCallSuffix, VRLFunction> {

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): VRLCallSuffix? {
        val callSuffix = callSuffixAt(context.file, context.offset) ?: return null
        val function = functionFor(callSuffix) ?: return null
        context.itemsToShow = arrayOf(function)
        return callSuffix
    }

    override fun showParameterInfo(element: VRLCallSuffix, context: CreateParameterInfoContext) {
        context.showHint(element, element.textRange.startOffset, this)
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): VRLCallSuffix? =
        callSuffixAt(context.file, context.offset)

    override fun updateParameterInfo(parameterOwner: VRLCallSuffix, context: UpdateParameterInfoContext) {
        if (context.parameterOwner !== parameterOwner) {
            context.parameterOwner = parameterOwner
        }
        context.setCurrentParameter(parameterIndexAt(parameterOwner, context.offset))
    }

    override fun updateUI(function: VRLFunction, context: ParameterInfoUIContext) {
        if (function.arguments.isEmpty()) {
            context.setupUIComponentPresentation("<no arguments>", 0, 0, false, false, false, context.defaultParameterColor)
            return
        }

        val text = StringBuilder()
        var highlightStart = 0
        var highlightEnd = 0
        function.arguments.forEachIndexed { index, argument ->
            if (index > 0) text.append(", ")
            val start = text.length
            text.append(presentArgument(argument))
            if (index == context.currentParameterIndex) {
                highlightStart = start
                highlightEnd = text.length
            }
        }

        context.setupUIComponentPresentation(text.toString(), highlightStart, highlightEnd, false, false, false, context.defaultParameterColor)
    }

    private fun presentArgument(argument: VRLFunctionArgument): String = buildString {
        append(argument.name)
        if (!argument.isRequired) append('?')
        append(": ")
        append(argument.types.sorted().joinToString("|"))
        argument.defaultValue?.let { append(" = ").append(formatDefaultValue(it)) }
    }

    private fun formatDefaultValue(value: Any): String = if (value is String) "\"$value\"" else value.toString()

    private fun callSuffixAt(file: PsiFile, offset: Int): VRLCallSuffix? {
        val element = file.findElementAt(offset) ?: file.findElementAt(offset - 1) ?: return null
        return PsiTreeUtil.getParentOfType(element, VRLCallSuffix::class.java)
    }

    private fun functionFor(callSuffix: VRLCallSuffix): VRLFunction? {
        val postfixSuffix = callSuffix.parent as? VRLPostfixSuffix ?: return null
        val postfixExpr = postfixSuffix.parent as? VRLPostfixExpr ?: return null
        if (postfixExpr.postfixSuffixList.firstOrNull() != postfixSuffix) return null
        val functionCall = postfixExpr.primaryExpr.node.findChildByType(VRLElementTypes.FUNCTION_CALL) ?: return null
        return allFunctions[functionCall.text]
    }

    private fun parameterIndexAt(callSuffix: VRLCallSuffix, offset: Int): Int {
        val arguments = callSuffix.argumentList?.argumentList ?: return 0
        for ((index, argument) in arguments.withIndex()) {
            if (offset <= argument.textRange.endOffset) return index
        }
        return arguments.size
    }
}
