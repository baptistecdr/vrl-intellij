package eu.bcosp.vrlintellij.refactoring

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import eu.bcosp.vrlintellij.grammars.VRLLexerAdapter
import eu.bcosp.vrlintellij.psi.VRLElementTypes

/**
 * Backs Rename's and Introduce Variable's "invalid identifier" checks with VRL's actual lexical
 * rules, so renaming a variable to a reserved word or an invalid identifier is rejected up front
 * instead of silently producing a script the parser then chokes on.
 *
 * Decided by running the plugin's own lexer over the name rather than keeping a second copy of
 * its identifier pattern and keyword lists: a name is a valid variable exactly when it lexes as a
 * single IDENTIFIER token, and a keyword when it lexes as a single keyword or reserved-identifier
 * token (vrl's lex.rs reports both as unusable for a variable).
 */
class VRLNamesValidator : NamesValidator {

    override fun isKeyword(name: String, project: Project?): Boolean = singleTokenType(name) in KEYWORD_TOKENS

    override fun isIdentifier(name: String, project: Project?): Boolean =
        singleTokenType(name) == VRLElementTypes.IDENTIFIER

    private fun singleTokenType(name: String): IElementType? {
        if (name.isEmpty()) return null
        val lexer = VRLLexerAdapter()
        lexer.start(name)
        val type = lexer.tokenType ?: return null
        if (lexer.tokenEnd != name.length) return null
        return type
    }

    companion object {
        private val KEYWORD_TOKENS = TokenSet.create(
            VRLElementTypes.IF, VRLElementTypes.ELSE, VRLElementTypes.TRUE, VRLElementTypes.FALSE,
            VRLElementTypes.NULL, VRLElementTypes.ABORT, VRLElementTypes.RETURN, VRLElementTypes.BREAK,
            VRLElementTypes.RESERVED_IDENTIFIER,
        )
    }
}
