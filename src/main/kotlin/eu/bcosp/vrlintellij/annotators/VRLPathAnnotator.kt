package eu.bcosp.vrlintellij.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import eu.bcosp.vrlintellij.colors.VRLColor
import eu.bcosp.vrlintellij.psi.VRLElementTypes
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
 * reserved words (`.if`, `.for`, `.string`) are valid field names too - see VRL.bnf's
 * `field_name`. Those would otherwise keep their keyword color in field position.
 */
class VRLPathAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is VRLPathSegment && element !is VRLPostfixSuffix) return
        val range = element.node.findChildByType(FIELD_NAME_TOKENS)?.textRange ?: return

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(VRLColor.PATH.textAttributesKey)
            .create()
    }

    private companion object {
        val FIELD_NAME_TOKENS = TokenSet.create(
            VRLElementTypes.IDENTIFIER, VRLElementTypes.PATH_FIELD, VRLElementTypes.RESERVED_IDENTIFIER,
            VRLElementTypes.STRING, VRLElementTypes.IF, VRLElementTypes.ELSE, VRLElementTypes.NULL,
            VRLElementTypes.TRUE, VRLElementTypes.FALSE, VRLElementTypes.ABORT, VRLElementTypes.RETURN,
            VRLElementTypes.BREAK,
        )
    }
}
