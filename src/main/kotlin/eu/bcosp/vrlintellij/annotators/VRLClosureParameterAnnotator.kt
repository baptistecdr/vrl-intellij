package eu.bcosp.vrlintellij.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import eu.bcosp.vrlintellij.colors.VRLColor
import eu.bcosp.vrlintellij.psi.VRLClosureParam
import eu.bcosp.vrlintellij.psi.VRLPrimaryExpr
import eu.bcosp.vrlintellij.references.VRLVariableResolver

/**
 * Lexer-level highlighting can't tell a closure parameter (`|k, v| { ... }`) apart from an
 * ordinary variable - both are plain IDENTIFIER tokens. This colors a parameter's declaration,
 * plus every bare-identifier read inside the closure body that [VRLVariableResolver] resolves
 * back to it, distinctly from [VRLColor.IDENTIFIER] - mirroring how [VRLPathAnnotator] already
 * separately colors field-access identifiers.
 */
class VRLClosureParameterAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val range = when (element) {
            is VRLClosureParam -> element.identifier.textRange
            is VRLPrimaryExpr -> {
                val identifier = element.identifier ?: return
                if (VRLVariableResolver.isBareAssignmentTarget(element)) return
                if (VRLVariableResolver.resolve(identifier) !is VRLClosureParam) return
                identifier.textRange
            }
            else -> return
        }

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(VRLColor.CLOSURE_PARAMETER.textAttributesKey)
            .create()
    }
}
