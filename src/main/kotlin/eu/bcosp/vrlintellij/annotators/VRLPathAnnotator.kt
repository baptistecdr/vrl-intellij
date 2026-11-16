package eu.bcosp.vrlintellij.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import eu.bcosp.vrlintellij.colors.VRLColor
import eu.bcosp.vrlintellij.psi.VRLFieldName
import eu.bcosp.vrlintellij.psi.VRLPathSegment
import eu.bcosp.vrlintellij.psi.VRLPostfixSuffix

/**
 * Lexer-level highlighting can't tell `.foo` (an event field) apart from `x` (a local
 * variable) or `x.foo` (a field access on a value) — both use the plain IDENTIFIER token.
 * This colors identifiers in field-access position (path segments and postfix `.field`
 * access) distinctly from bare variable identifiers. Path segments can also be a quoted
 * string (`."my key"`, `x."bar baz"`) - `%` metadata paths reuse the same [VRLPathSegment]
 * rule as `.` event paths, so they're covered by the same branch without extra handling.
 *
 * A field name isn't always an IDENTIFIER token: `.@timestamp` is a PATH_FIELD, and keywords or
 * reserved words (`.if`, `.for`, `.string`) are valid field names too. VRL.bnf wraps every one of
 * them in a [VRLFieldName] node, so this colors that node rather than re-listing the tokens -
 * which would otherwise keep their keyword color in field position.
 */
class VRLPathAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val range = when (element) {
            is VRLFieldName -> element.textRange
            is VRLPathSegment -> element.string?.textRange
            is VRLPostfixSuffix -> element.string?.textRange
            else -> null
        } ?: return

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(VRLColor.PATH.textAttributesKey)
            .create()
    }
}
