package eu.bcosp.vrlintellij.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import eu.bcosp.vrlintellij.colors.VRLColor
import eu.bcosp.vrlintellij.psi.VRLPathSegment
import eu.bcosp.vrlintellij.psi.VRLPostfixSuffix

/**
 * Lexer-level highlighting can't tell `.foo` (an event field) apart from `x` (a local
 * variable) or `x.foo` (a field access on a value) — both use the plain IDENTIFIER token.
 * This colors identifiers in field-access position (path segments and postfix `.field`
 * access) distinctly from bare variable identifiers. Path segments can also be a quoted
 * string (`."my key"`, `x."bar baz"`) - `%` metadata paths reuse the same [VRLPathSegment]
 * rule as `.` event paths, so they're covered by the same branch without extra handling.
 */
class VRLPathAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val range = when (element) {
            is VRLPathSegment -> element.identifier?.textRange ?: element.string?.textRange
            is VRLPostfixSuffix -> element.identifier?.textRange ?: element.string?.textRange
            else -> null
        } ?: return

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(VRLColor.PATH.textAttributesKey)
            .create()
    }
}
