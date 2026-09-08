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
        // Mirrors VRLLexer.flex's IDENTIFIER token pattern exactly.
        private val IDENTIFIER_PATTERN = Regex("[a-zA-Z_][a-zA-Z0-9_]*")

        // Mirrors vrl's own lexer (src/parser/lex.rs) exactly - both its keyword literals and its
        // separate `ReservedIdentifier` list (words held back for future syntax, e.g. `string`,
        // `object`, `timestamp` - several of which double as stdlib function names, which stay
        // callable since this only governs variable/rename identifiers). `in` is deliberately
        // absent: it reads like an operator but isn't reserved (`vector vrl 'in = 1'` compiles).
        private val KEYWORDS = setOf(
            "if", "else", "true", "false", "null", "abort", "return",
        )
        private val RESERVED_IDENTIFIERS = setOf(
            "array", "bool", "boolean", "break", "continue", "do", "emit", "float",
            "for", "forall", "foreach", "all", "each", "any", "try", "undefined",
            "int", "integer", "iter", "object", "regex", "string", "traverse",
            "timestamp", "duration", "unless", "walk", "while", "loop",
        )
        private val RESERVED_WORDS = KEYWORDS + RESERVED_IDENTIFIERS
    }
}
