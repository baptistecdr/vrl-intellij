package eu.bcosp.vrlintellij.formatting

import com.intellij.formatting.Alignment
import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.FormattingModelProvider
import com.intellij.formatting.Indent
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.formatting.WrapType
import com.intellij.psi.codeStyle.CodeStyleSettings
import eu.bcosp.vrlintellij.VRL
import eu.bcosp.vrlintellij.psi.VRLElementTypes

class VRLFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val settings = formattingContext.codeStyleSettings
        val rootBlock = VRLBlock(
            formattingContext.node,
            Wrap.createWrap(WrapType.NONE, false),
            Alignment.createAlignment(),
            Indent.getNoneIndent(),
            createSpacingBuilder(settings),
            settings.getCommonSettings(VRL).KEEP_BLANK_LINES_IN_CODE,
        )
        return FormattingModelProvider.createFormattingModelForPsiFile(formattingContext.containingFile, rootBlock, settings)
    }

    private fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
        val common = settings.getCommonSettings(VRL)
        return SpacingBuilder(settings, VRL)
            // Punctuation
            .after(VRLElementTypes.LPAREN).spaces(0)
            .before(VRLElementTypes.RPAREN).spaces(0)
            .after(VRLElementTypes.LBRACKET).spaces(0)
            .before(VRLElementTypes.RBRACKET).spaces(0)
            .before(VRLElementTypes.LBRACE).spaces(1)
            .before(VRLElementTypes.COMMA).spaces(if (common.SPACE_BEFORE_COMMA) 1 else 0)
            .after(VRLElementTypes.COMMA).spaces(if (common.SPACE_AFTER_COMMA) 1 else 0)
            .before(VRLElementTypes.SEMICOLON).spaces(0)
            .before(VRLElementTypes.COLON).spaces(0)
            .after(VRLElementTypes.COLON).spaces(1)

            // Paths (`.foo.bar`, `%tag`) stay tight - no spaces around the segment dots.
            .aroundInside(VRLElementTypes.DOT, VRLElementTypes.PATH_EXPR).spaces(0)
            .aroundInside(VRLElementTypes.DOT, VRLElementTypes.POSTFIX_SUFFIX).spaces(0)
            .aroundInside(VRLElementTypes.PERCENT, VRLElementTypes.METADATA_PATH_EXPR).spaces(0)

            // Keywords
            .after(VRLElementTypes.IF).spaces(1)
            .after(VRLElementTypes.ELSE).spaces(1)
            .after(VRLElementTypes.RETURN).spaces(1)
            .after(VRLElementTypes.ABORT).spaces(1)
            .before(VRLElementTypes.IN).spaces(1)
            .after(VRLElementTypes.IN).spaces(1)

            // Closures: `-> |param| { ... }`
            .around(VRLElementTypes.ARROW).spaces(1)

            // Assignment operators
            .aroundInside(VRLElementTypes.ASSIGN, VRLElementTypes.ASSIGNMENT_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.MERGE_ASSIGN, VRLElementTypes.ASSIGNMENT_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.ASSIGN, VRLElementTypes.MULTI_ASSIGNMENT_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.MERGE_ASSIGN, VRLElementTypes.MULTI_ASSIGNMENT_EXPR).spaces(1)

            // Binary operators - scoped to their precedence level's node so unary `!`/`-` (which
            // share tokens with NOT/MINUS here) are left untouched.
            .aroundInside(VRLElementTypes.OR, VRLElementTypes.OR_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.AND, VRLElementTypes.AND_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.NULL_COALESCE, VRLElementTypes.NULL_COALESCE_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.EQ, VRLElementTypes.COMPARISON_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.NE, VRLElementTypes.COMPARISON_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.GT, VRLElementTypes.COMPARISON_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.GE, VRLElementTypes.COMPARISON_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.LT, VRLElementTypes.COMPARISON_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.LE, VRLElementTypes.COMPARISON_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.PLUS, VRLElementTypes.ADDITIVE_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.MINUS, VRLElementTypes.ADDITIVE_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.STAR, VRLElementTypes.MULTIPLICATIVE_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.SLASH, VRLElementTypes.MULTIPLICATIVE_EXPR).spaces(1)
            .aroundInside(VRLElementTypes.PERCENT, VRLElementTypes.MULTIPLICATIVE_EXPR).spaces(1)
    }
}
