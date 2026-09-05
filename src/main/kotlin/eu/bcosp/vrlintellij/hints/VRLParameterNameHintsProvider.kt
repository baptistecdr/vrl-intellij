package eu.bcosp.vrlintellij.hints

import com.intellij.codeInsight.hints.declarative.HintFormat
import com.intellij.codeInsight.hints.declarative.InlayHintsCollector
import com.intellij.codeInsight.hints.declarative.InlayHintsProvider
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition
import com.intellij.codeInsight.hints.declarative.SharedBypassCollector
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import eu.bcosp.vrlintellij.functions.allFunctions
import eu.bcosp.vrlintellij.psi.VRLArgument
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLPostfixExpr
import eu.bcosp.vrlintellij.psi.VRLPrimaryExpr

/**
 * Shows each positional argument's parameter name inline, e.g. `slice(., <start:>0, <end:>5)`,
 * for calls to functions declaring more than one parameter - the case where position alone
 * doesn't make an argument's role obvious. Skipped entirely for single-parameter functions (the
 * one argument's role is never in question), and per-argument whenever the argument's own text
 * already spells out the parameter name (`upcase(value)`), where the hint would just repeat what's
 * already on screen.
 */
class VRLParameterNameHintsProvider : InlayHintsProvider {
    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector = Collector

    private object Collector : SharedBypassCollector {
        override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
            if (element.node?.elementType != VRLElementTypes.FUNCTION_CALL) return
            val function = allFunctions[element.text] ?: return
            if (function.arguments.size <= 1) return
            val arguments = callArguments(element) ?: return

            var positionalIndex = 0
            for (argument in arguments) {
                if (argument.isNamed()) continue
                val parameter = function.arguments.getOrNull(positionalIndex)
                positionalIndex++
                if (parameter == null) continue
                if (argument.assignmentExpr?.text == parameter.name) continue
                addHint(sink, argument.textRange.startOffset, parameter.name)
            }
        }

        private fun callArguments(functionCall: PsiElement): List<VRLArgument>? {
            val primaryExpr = functionCall.parent as? VRLPrimaryExpr ?: return null
            val postfixExpr = primaryExpr.parent as? VRLPostfixExpr ?: return null
            val callSuffix = postfixExpr.postfixSuffixList.firstOrNull()?.callSuffix ?: return null
            return callSuffix.argumentList?.argumentList ?: emptyList()
        }

        // A named argument is `IDENTIFIER COLON expression`; a bare identifier used positionally
        // also has a non-null `identifier`, so the COLON must be checked too.
        private fun VRLArgument.isNamed(): Boolean =
            identifier != null && node.findChildByType(VRLElementTypes.COLON) != null

        private fun addHint(sink: InlayTreeSink, offset: Int, parameterName: String) {
            sink.addPresentation(
                InlineInlayPosition(offset, relatedToPrevious = false),
                tooltip = null,
                hintFormat = HintFormat.default,
            ) {
                text("$parameterName:")
            }
        }
    }
}
