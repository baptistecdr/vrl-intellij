package eu.bcosp.vrlintellij.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import eu.bcosp.vrlintellij.colors.VRLColor

class VRLHighlighter: SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer {
        return VRLHighlightingLexer()
    }

    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        if (tokenType == null) return emptyArray()
        val attributeKey = tokenSetMap(tokenType)?.textAttributesKey
        if (attributeKey != null) return pack(attributeKey)
        return emptyArray()
    }

    private fun tokenSetMap(tokenType: IElementType?): VRLColor? = when (tokenType) {
        TokenType.BAD_CHARACTER -> VRLColor.ILLEGAL
        in VRLHighlightingTokenSets.FUNCTION_CALL -> VRLColor.FUNCTION_CALL
        in VRLHighlightingTokenSets.COMMENT -> VRLColor.LINE_COMMENT
        in VRLHighlightingTokenSets.KEYWORD -> VRLColor.KEYWORD
        in VRLHighlightingTokenSets.OPERATOR -> VRLColor.OPERATOR
        in VRLHighlightingTokenSets.PARENTHESES -> VRLColor.PARENTHESES
        in VRLHighlightingTokenSets.BRACES -> VRLColor.BRACES
        in VRLHighlightingTokenSets.BRACKETS -> VRLColor.BRACKETS
        in VRLHighlightingTokenSets.COLON -> VRLColor.COLON
        in VRLHighlightingTokenSets.SEMICOLON -> VRLColor.SEMICOLON
        in VRLHighlightingTokenSets.NUMBER_LITERAL -> VRLColor.NUMBER
        in VRLHighlightingTokenSets.STRING_LITERAL -> VRLColor.STRING
        in VRLHighlightingTokenSets.COMMA -> VRLColor.COMMA
        in VRLHighlightingTokenSets.DOT -> VRLColor.DOT
        in VRLHighlightingTokenSets.IDENTIFIER -> VRLColor.IDENTIFIER
        in VRLHighlightingTokenSets.ARROW -> VRLColor.ARROW
        else -> null
    }
}
