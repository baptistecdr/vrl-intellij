package eu.bcosp.vrlintellij.injection

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * VRL compiles every `r'...'` literal with Rust's `regex` crate, which is stricter than the
 * platform's default "accept everything" RegExp dialect - each case here was confirmed against
 * `vector vrl` directly (see [VRLRegExpLanguageHost]'s doc comment for the exact evidence).
 */
class VRLRegExpLanguageHostTest : BasePlatformTestCase() {

    private fun descriptions(text: String): List<String> {
        myFixture.configureByText("t.vrl", text)
        return myFixture.doHighlighting().mapNotNull { it.description }
    }

    fun testFlagsPositiveLookbehindAsUnsupported() {
        val messages = descriptions("x = r'(?<=a)b'\n")
        assertTrue(messages.any { it.contains("Look-behind groups are not supported in this regex dialect") })
    }

    fun testFlagsNegativeLookbehindAsUnsupported() {
        val messages = descriptions("x = r'(?<!a)b'\n")
        assertTrue(messages.any { it.contains("Look-behind groups are not supported in this regex dialect") })
    }

    fun testFlagsPerl5EmbeddedCommentAsUnsupported() {
        val messages = descriptions("x = r'(?#comment)a'\n")
        assertTrue(messages.any { it.contains("Embedded comments are not supported in this regex dialect") })
    }

    // Atomic groups share VRL/Rust's "no backtracking control" gap with possessive quantifiers -
    // (?>...) is rejected by `vector vrl` as an unrecognized flag.
    fun testFlagsAtomicGroupAsUnsupported() {
        val messages = descriptions("x = r'(?>a)'\n")
        assertTrue(messages.any { it.contains("Atomic groups are not supported in this regex dialect") })
    }

    fun testFlagsNamedGroupBackreferenceAsUnsupported() {
        val messages = descriptions("x = r'(?<n>a)\\k<n>'\n")
        assertTrue(messages.any { it.contains("This named group reference syntax is not supported in this regex dialect") })
    }

    fun testDoesNotFlagAngleBracketNamedGroupSyntax() {
        val messages = descriptions("x = r'(?<n>a)'\n")
        assertTrue(messages.none { it.contains("named group syntax is not supported") })
    }

    fun testDoesNotFlagPythonNamedGroupSyntax() {
        val messages = descriptions("x = r'(?P<n>a)'\n")
        assertTrue(messages.none { it.contains("named group syntax is not supported") })
    }

    fun testDoesNotFlagBracedHexEscape() {
        assertTrue(descriptions("x = r'\\x{1F600}'\n").isEmpty())
    }

    fun testDoesNotFlagUnicodeProperty() {
        assertTrue(descriptions("x = r'\\p{L}'\n").isEmpty())
    }
}
