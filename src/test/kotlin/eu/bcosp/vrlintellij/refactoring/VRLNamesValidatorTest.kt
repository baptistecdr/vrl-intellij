package eu.bcosp.vrlintellij.refactoring

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VRLNamesValidatorTest {

    private val validator = VRLNamesValidator()

    @Test
    fun `accepts a plain identifier`() {
        assertTrue(validator.isIdentifier("foo", null))
        assertTrue(validator.isIdentifier("_foo", null))
        assertTrue(validator.isIdentifier("foo_bar_123", null))
    }

    @Test
    fun `rejects an identifier starting with a digit`() {
        assertFalse(validator.isIdentifier("1foo", null))
    }

    @Test
    fun `rejects an empty name`() {
        assertFalse(validator.isIdentifier("", null))
    }

    @Test
    fun `rejects names with invalid characters`() {
        assertFalse(validator.isIdentifier("foo-bar", null))
        assertFalse(validator.isIdentifier("foo.bar", null))
        assertFalse(validator.isIdentifier("foo bar", null))
    }

    @Test
    fun `rejects reserved keywords as identifiers`() {
        for (keyword in listOf("if", "else", "for", "while", "loop", "break", "continue", "return", "abort", "true", "false", "null")) {
            assertFalse("'$keyword' should not be a valid identifier", validator.isIdentifier(keyword, null))
            assertTrue("'$keyword' should be recognized as a keyword", validator.isKeyword(keyword, null))
        }
    }

    // vrl's lexer holds these back as `ReservedIdentifier`s for future syntax (src/parser/lex.rs)
    // even though they're not control-flow keywords - `vector vrl 'string = 1'` is a syntax error.
    @Test
    fun `rejects reserved identifiers as identifiers`() {
        for (word in listOf(
            "array", "bool", "boolean", "do", "emit", "float", "forall", "all", "each", "any",
            "try", "undefined", "int", "integer", "iter", "object", "regex", "string", "traverse",
            "timestamp", "duration", "unless", "walk", "foreach",
        )) {
            assertFalse("'$word' should not be a valid identifier", validator.isIdentifier(word, null))
            assertTrue("'$word' should be recognized as a keyword", validator.isKeyword(word, null))
        }
    }

    // Reads like an operator but VRL never reserved it - `vector vrl 'in = 1\nin + 1'` compiles.
    @Test
    fun `does not treat 'in' as reserved`() {
        assertTrue(validator.isIdentifier("in", null))
        assertFalse(validator.isKeyword("in", null))
    }

    @Test
    fun `does not treat an ordinary identifier as a keyword`() {
        assertFalse(validator.isKeyword("foo", null))
    }
}
