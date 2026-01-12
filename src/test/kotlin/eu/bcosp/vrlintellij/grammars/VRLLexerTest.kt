package eu.bcosp.vrlintellij.grammars

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import junit.framework.TestCase

class VRLLexerTest : TestCase() {

    private fun tokenize(text: String): List<Pair<IElementType, String>> {
        val lexer = VRLLexerAdapter()
        lexer.start(text)
        val tokens = mutableListOf<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            tokens.add(lexer.tokenType!! to text.substring(lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
        return tokens
    }

    private fun nonTrivialTokens(text: String) =
        tokenize(text).filter { it.first != TokenType.WHITE_SPACE }

    fun testPlainIdentifierIsNotFunctionCall() {
        assertEquals(
            listOf(VRLElementTypes.IDENTIFIER to "foo_bar"),
            nonTrivialTokens("foo_bar")
        )
    }

    fun testIdentifierFollowedByParenIsFunctionCall() {
        assertEquals(
            listOf(
                VRLElementTypes.FUNCTION_CALL to "upcase",
                VRLElementTypes.LPAREN to "(",
                VRLElementTypes.STRING to "\"x\"",
                VRLElementTypes.RPAREN to ")",
            ),
            nonTrivialTokens("upcase(\"x\")")
        )
    }

    fun testFallibleCallBangIsSeparateFromFunctionCallToken() {
        val tokens = nonTrivialTokens("parse_json!(.msg)")
        assertEquals(VRLElementTypes.FUNCTION_CALL to "parse_json", tokens[0])
        assertEquals(VRLElementTypes.NOT to "!", tokens[1])
        assertEquals(VRLElementTypes.LPAREN to "(", tokens[2])
    }

    fun testInIsAKeywordNotAnIdentifier() {
        assertEquals(
            listOf(
                VRLElementTypes.IDENTIFIER to "a",
                VRLElementTypes.IN to "in",
                VRLElementTypes.IDENTIFIER to "b",
            ),
            nonTrivialTokens("a in b")
        )
    }

    fun testStringLiteral() {
        assertEquals(listOf(VRLElementTypes.STRING to "\"hello\""), nonTrivialTokens("\"hello\""))
    }

    fun testUnterminatedStringLexesAsStringNotBadCharacter() {
        // The closing quote is deliberately optional: this is what lets a freshly-typed opening
        // quote (before its pair exists) still lex as STRING, which VRLQuoteHandler needs to
        // recognize the position and auto-insert the closing quote.
        assertEquals(listOf(VRLElementTypes.STRING to "\"hello"), nonTrivialTokens("\"hello"))
        assertEquals(listOf(VRLElementTypes.STRING to "\""), nonTrivialTokens("\""))
    }

    fun testRawStringLiteral() {
        assertEquals(listOf(VRLElementTypes.RAW_STRING to "s'hello'"), nonTrivialTokens("s'hello'"))
    }

    fun testRawStringLiteralWithEscapedQuote() {
        // VRL escapes an embedded quote with a backslash (not by doubling it), matching the
        // real vrl-lang lexer's `quoted_literal` handling for s'...'/r'.../t'...'.
        assertEquals(
            listOf(VRLElementTypes.RAW_STRING to "s'it\\'s raining'"),
            nonTrivialTokens("s'it\\'s raining'")
        )
    }

    fun testRegexLiteral() {
        assertEquals(listOf(VRLElementTypes.REGEX to "r'^[a-z]+'"), nonTrivialTokens("r'^[a-z]+'"))
    }

    fun testRegexLiteralWithEscapedQuote() {
        assertEquals(
            listOf(VRLElementTypes.REGEX to "r'can\\'t'"),
            nonTrivialTokens("r'can\\'t'")
        )
    }

    fun testTimestampLiteral() {
        assertEquals(
            listOf(VRLElementTypes.TIMESTAMP to "t'2021-01-01T00:00:00Z'"),
            nonTrivialTokens("t'2021-01-01T00:00:00Z'")
        )
    }

    fun testLineComment() {
        assertEquals(listOf(VRLElementTypes.COMMENT to "# hello"), nonTrivialTokens("# hello"))
    }

    fun testIntegerAndFloatLiterals() {
        assertEquals(
            listOf(VRLElementTypes.INTEGER_LITERAL to "42", VRLElementTypes.FLOAT_LITERAL to "1.5"),
            nonTrivialTokens("42 1.5")
        )
    }

    fun testKeywords() {
        val text = "if else for while loop break continue return abort true false null in"
        val expectedTypes = listOf(
            VRLElementTypes.IF, VRLElementTypes.ELSE, VRLElementTypes.FOR, VRLElementTypes.WHILE,
            VRLElementTypes.LOOP, VRLElementTypes.BREAK, VRLElementTypes.CONTINUE, VRLElementTypes.RETURN,
            VRLElementTypes.ABORT, VRLElementTypes.TRUE, VRLElementTypes.FALSE, VRLElementTypes.NULL,
            VRLElementTypes.IN,
        )
        assertEquals(expectedTypes, nonTrivialTokens(text).map { it.first })
    }

    fun testArrowToken() {
        assertEquals(listOf(VRLElementTypes.ARROW to "->"), nonTrivialTokens("->"))
    }
}
