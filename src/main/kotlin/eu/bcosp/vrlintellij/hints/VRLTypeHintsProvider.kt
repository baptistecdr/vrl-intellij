package eu.bcosp.vrlintellij.hints

import com.intellij.codeInsight.hints.declarative.HintFormat
import com.intellij.codeInsight.hints.declarative.InlayHintsCollector
import com.intellij.codeInsight.hints.declarative.InlayHintsProvider
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition
import com.intellij.codeInsight.hints.declarative.SharedBypassCollector
import com.intellij.lang.ASTNode
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import eu.bcosp.vrlintellij.functions.VRLFunction
import eu.bcosp.vrlintellij.functions.allFunctions
import eu.bcosp.vrlintellij.psi.VRLAssignmentExpr
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLMultiAssignmentExpr

/**
 * Shows the return type of a directly-assigned function call inline, e.g. `x: string = parse_json(...)`,
 * using the return types already recorded per-function in [allFunctions]. Only fires when the RHS is
 * *directly* a bare function call (no `+`, no `.field` chaining, no `??`, ...) - anything else has no
 * single reliable type to show. For VRL's error-destructuring form (`value, err = fallible_call()`),
 * both targets get a hint: the success types (minus VRL's own "error" pseudo-type) for `value`, and
 * literal "error" for `err`.
 */
class VRLTypeHintsProvider : InlayHintsProvider {
    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector = Collector

    private object Collector : SharedBypassCollector {
        override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
            when (element) {
                is VRLMultiAssignmentExpr -> {
                    val targets = element.orExprList
                    if (targets.size != 2) return
                    val function = resolveDirectCallReturnType(element.assignmentExpr.node) ?: return
                    addHint(sink, targets[0].textRange.endOffset, successTypes(function))
                    addHint(sink, targets[1].textRange.endOffset, "error")
                }

                is VRLAssignmentExpr -> {
                    val rhs = element.assignmentExpr ?: return
                    val function = resolveDirectCallReturnType(rhs.node) ?: return
                    addHint(sink, element.orExpr.textRange.endOffset, successTypes(function))
                }
            }
        }

        private fun resolveDirectCallReturnType(rhsRoot: ASTNode): VRLFunction? {
            val functionCall = functionCallToken(rhsRoot) ?: return null
            return allFunctions[functionCall.text]
        }

        private fun successTypes(function: VRLFunction): String =
            (function.returnTypes - "error").sorted().joinToString("|").ifEmpty { "any" }

        private fun addHint(sink: InlayTreeSink, offset: Int, typeText: String) {
            sink.addPresentation(
                InlineInlayPosition(offset, relatedToPrevious = true),
                tooltip = null,
                hintFormat = HintFormat.default,
            ) {
                text(": $typeText")
            }
        }

        /** Descends from `root` through every "pass-through" wrapper (a node with exactly one
         * significant child) down to a bare, unchained function call - `FUNCTION_CALL(...)` with
         * no leading operator and no trailing `.field`/`[idx]` postfix. Returns null for anything
         * else (`a + b()`, `x`, `f() ?? g()`, `f().field`, ...), since there's no single type to
         * attribute those to. */
        private fun functionCallToken(root: ASTNode): ASTNode? {
            var current = root
            while (current.elementType != VRLElementTypes.POSTFIX_EXPR) {
                current = onlySignificantChild(current) ?: return null
            }
            val children = significantChildren(current)
            if (children.size != 2) return null
            if (children[0].elementType != VRLElementTypes.PRIMARY_EXPR) return null
            val functionCall = onlySignificantChild(children[0])
                ?.takeIf { it.elementType == VRLElementTypes.FUNCTION_CALL } ?: return null
            if (children[1].elementType != VRLElementTypes.POSTFIX_SUFFIX) return null
            onlySignificantChild(children[1])
                ?.takeIf { it.elementType == VRLElementTypes.CALL_SUFFIX } ?: return null
            return functionCall
        }

        private fun onlySignificantChild(node: ASTNode): ASTNode? = significantChildren(node).singleOrNull()

        private fun significantChildren(node: ASTNode): List<ASTNode> = node.getChildren(null).filterNot {
            it.elementType == TokenType.WHITE_SPACE || it.elementType == VRLElementTypes.COMMENT
        }
    }
}
