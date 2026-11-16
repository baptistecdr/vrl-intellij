package eu.bcosp.vrlintellij.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
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

    // PsiBuilder skips whitespace lazily: right after advanceLexer() it still sits just past the
    // consumed token, so rawLookup(-1) would return that token even when whitespace follows it
    // (`x. b` looked adjacent). Asking for the current token type moves past the whitespace first.
    private fun skipToCurrentToken(builder: PsiBuilder) {
        builder.tokenType
    }
}
