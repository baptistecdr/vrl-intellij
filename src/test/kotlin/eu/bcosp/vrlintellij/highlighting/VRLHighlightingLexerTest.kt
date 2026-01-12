package eu.bcosp.vrlintellij.highlighting

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import junit.framework.TestCase

class VRLHighlightingLexerTest : TestCase() {

    private fun tokenize(text: String): List<Pair<IElementType, String>> {
        val lexer = VRLHighlightingLexer()
        lexer.start(text)
        val tokens = mutableListOf<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            tokens.add(lexer.tokenType!! to text.substring(lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
        return tokens
    }

    fun testTemplateVariableInsideStringIsHighlightedSeparately() {
        assertEquals(
            listOf(
                VRLElementTypes.STRING to "\"Hello, ",
                TEMPLATE_START to "{{",
                TokenType.WHITE_SPACE to " ",
                TEMPLATE_VARIABLE to "planet",
                TokenType.WHITE_SPACE to " ",
                TEMPLATE_END to "}}",
                VRLElementTypes.STRING to "!\"",
            ),
            tokenize("\"Hello, {{ planet }}!\"")
        )
    }

    fun testStringWithoutTemplateIsUnaffected() {
        assertEquals(
            listOf(VRLElementTypes.STRING to "\"hello\""),
            tokenize("\"hello\"")
        )
    }

    fun testEscapedBraceIsNotTreatedAsTemplateStart() {
        val text = "\"literal \\{ brace\""
        val tokens = tokenize(text)
        assertTrue(tokens.all { it.first == VRLElementTypes.STRING })
        assertEquals(text, tokens.joinToString("") { it.second })
    }

    fun testRawStringIsNotLayeredForTemplates() {
        // Interpolation only applies to interpreted "..." strings, not raw s'...' strings.
        assertEquals(
            listOf(VRLElementTypes.RAW_STRING to "s'{{ not a template }}'"),
            tokenize("s'{{ not a template }}'")
        )
    }

    fun testFullTextIsPreservedAcrossLayeredTokens() {
        val text = "x = \"Hello, {{ planet }}!\""
        val reconstructed = tokenize(text).joinToString("") { it.second }
        assertEquals(text, reconstructed)
    }
}
