package eu.bcosp.vrlintellij.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.WhitespacesBinders
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.lang.parser.GeneratedParserUtilBase.Parser
import com.intellij.psi.TokenType
import eu.bcosp.vrlintellij.psi.NEWLINE
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.isWhitespaceOrComment

/**
 * Grammar-Kit external predicate, invoked as `<<newlineBefore>>` in VRL.bnf. The generated parser
 * resolves that call via `import static eu.bcosp.vrlintellij.parser.VRLParserUtil.*;`, which is
 * also how it reaches ordinary parsing helpers like `consumeToken`/`exit_section_` once
 * `parserUtilClass` is set to this class instead of the default `GeneratedParserUtilBase` -
 * extending that base here (rather than plain `object VRLParserUtil`) is what keeps those
 * inherited static members visible to that same wildcard import.
 *
 * `NEWLINE` stays registered as a whitespace token (see VRLParserDefinition.getWhitespaceTokens)
 * so it's silently skipped everywhere by default; this predicate is the one place that looks past
 * that skip - via [PsiBuilder.rawLookup], which sees raw tokens the normal (whitespace-skipping)
 * builder API doesn't - to tell whether a genuine line break separates the current position from
 * whatever precedes it, as opposed to plain horizontal whitespace or nothing at all.
 */
object VRLParserUtil : GeneratedParserUtilBase() {

    @JvmStatic
    fun newlineBefore(builder: PsiBuilder, level: Int): Boolean {
        skipToCurrentToken(builder)
        var steps = -1
        while (true) {
            when (builder.rawLookup(steps)) {
                null -> return false
                NEWLINE -> return true
                TokenType.WHITE_SPACE, VRLElementTypes.COMMENT -> steps--
                else -> return false
            }
        }
    }

    /**
     * `<<adjacent>>` in VRL.bnf: true when the current token directly follows the previous one,
     * with no whitespace, newline or comment between them - used where vrl's lexer only keeps a
     * path going on an adjacent character (`.a.b`, `.a[0]`, but not `.a .b`).
     */
    @JvmStatic
    fun adjacent(builder: PsiBuilder, level: Int): Boolean {
        skipToCurrentToken(builder)
        val previous = builder.rawLookup(-1) ?: return false
        return !isWhitespaceOrComment(previous)
    }

    /**
     * `<<statementEnd>>` in VRL.bnf: a statement must be followed by `;`, a line break, the
     * closing `}` of its block, or the end of the file (vrl's parser.lalrpop EndOfExpression).
     * When none follows, this reports one short error at the gap and still returns true - so the
     * statement keeps its own node and the next one parses normally - instead of Grammar-Kit's
     * generic "<every token that could have continued the expression> expected" message.
     */
    @JvmStatic
    fun statementEnd(builder: PsiBuilder, level: Int): Boolean {
        skipToCurrentToken(builder)
        when {
            builder.eof() || builder.tokenType == VRLElementTypes.RBRACE -> {}
            builder.tokenType == VRLElementTypes.SEMICOLON -> builder.advanceLexer()
            newlineBefore(builder, level) -> {}
            else -> {
                // Anchored right after the statement rather than at the next token, so the
                // statement's own range doesn't stretch over the whitespace before the gap.
                val error = builder.mark()
                error.error("';' or new line expected")
                error.setCustomEdgeTokenBinders(WhitespacesBinders.GREEDY_LEFT_BINDER, WhitespacesBinders.GREEDY_LEFT_BINDER)
            }
        }
        return true
    }

    /**
     * `<<afterFunctionName>>` in VRL.bnf: an argument list only ever follows a FUNCTION_CALL token
     * directly (vrl's lexer only makes one when `(` or `!` touches the name), so `.a(1)`,
     * `5xx(1)` and `f()(1)` are E203 rather than calls.
     */
    @JvmStatic
    fun afterFunctionName(builder: PsiBuilder, level: Int): Boolean {
        skipToCurrentToken(builder)
        return builder.rawLookup(-1) == VRLElementTypes.FUNCTION_CALL
    }

    /**
     * `<<sameLineValue expression>>` in VRL.bnf: parses [value] when it starts on the current line,
     * otherwise reports "expression expected" itself and succeeds - Grammar-Kit's own pinned
     * error is dropped when the value is missing at the very end of the file.
     */
    @JvmStatic
    fun sameLineValue(builder: PsiBuilder, level: Int, value: Parser): Boolean {
        skipToCurrentToken(builder)
        if (!builder.eof() && !newlineBefore(builder, level) && value.parse(builder, level)) return true
        val error = builder.mark()
        error.error("expression expected")
        error.setCustomEdgeTokenBinders(WhitespacesBinders.GREEDY_LEFT_BINDER, WhitespacesBinders.GREEDY_LEFT_BINDER)
        return true
    }

    // PsiBuilder skips whitespace lazily: right after advanceLexer() it still sits just past the
    // consumed token, so rawLookup(-1) would return that token even when whitespace follows it
    // (`x. b` looked adjacent). Asking for the current token type moves past the whitespace first.
    private fun skipToCurrentToken(builder: PsiBuilder) {
        builder.tokenType
    }
}
