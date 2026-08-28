package eu.bcosp.vrlintellij.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.psi.TokenType
import eu.bcosp.vrlintellij.psi.NEWLINE
import eu.bcosp.vrlintellij.psi.VRLElementTypes

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
}
