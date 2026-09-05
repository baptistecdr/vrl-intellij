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
        for (keyword in listOf("if", "else", "for", "while", "loop", "break", "continue", "return", "abort", "true", "false", "null", "in")) {
            assertFalse("'$keyword' should not be a valid identifier", validator.isIdentifier(keyword, null))
            assertTrue("'$keyword' should be recognized as a keyword", validator.isKeyword(keyword, null))
        }
    }

    @Test
    fun `does not treat an ordinary identifier as a keyword`() {
        assertFalse(validator.isKeyword("foo", null))
    }
}
