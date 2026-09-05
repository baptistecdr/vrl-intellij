package eu.bcosp.vrlintellij.refactoring

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project

/**
 * Backs Rename's and Introduce Variable's "invalid identifier" checks with VRL's actual lexical
 * rules, so renaming a variable to a reserved word or an invalid identifier is rejected up front
 * instead of silently producing a script the parser then chokes on.
 */
class VRLNamesValidator : NamesValidator {

    override fun isKeyword(name: String, project: Project?): Boolean = name in RESERVED_WORDS

    override fun isIdentifier(name: String, project: Project?): Boolean =
        IDENTIFIER_PATTERN.matches(name) && name !in RESERVED_WORDS

    companion object {
        // Mirrors VRLLexer.flex's IDENTIFIER token pattern and keyword literals exactly.
        private val IDENTIFIER_PATTERN = Regex("[a-zA-Z_][a-zA-Z0-9_]*")
        private val RESERVED_WORDS = setOf(
            "if", "else", "for", "while", "loop", "break", "continue",
            "return", "abort", "true", "false", "null", "in",
        )
    }
}
