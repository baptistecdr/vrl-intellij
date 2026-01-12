package eu.bcosp.vrlintellij.highlighting

import com.intellij.psi.tree.TokenSet
import eu.bcosp.vrlintellij.psi.VRLElementTypes

object VRLHighlightingTokenSets {
    val COMMENT = TokenSet.create(VRLElementTypes.COMMENT)

    val COLON = TokenSet.create(VRLElementTypes.COLON)
    val SEMICOLON = TokenSet.create(VRLElementTypes.SEMICOLON)
    val COMMA = TokenSet.create(VRLElementTypes.COMMA)
    val DOT = TokenSet.create(VRLElementTypes.DOT)

    val ARROW = TokenSet.create(VRLElementTypes.ARROW)
    val IDENTIFIER = TokenSet.create(VRLElementTypes.IDENTIFIER)

    val FUNCTION_CALL = TokenSet.create(VRLElementTypes.FUNCTION_CALL)

    val PARENTHESES = TokenSet.create(
        VRLElementTypes.LPAREN,
        VRLElementTypes.RPAREN,
    )

    val BRACKETS = TokenSet.create(
        VRLElementTypes.LBRACKET,
        VRLElementTypes.RBRACKET,
    )

    val BRACES = TokenSet.create(
        VRLElementTypes.LBRACE,
        VRLElementTypes.RBRACE,
    )

    val STRING_LITERAL = TokenSet.create(
        VRLElementTypes.STRING,
        VRLElementTypes.RAW_STRING
    )

    val NUMBER_LITERAL = TokenSet.create(
        VRLElementTypes.INTEGER_LITERAL,
        VRLElementTypes.FLOAT_LITERAL,
    )

    val KEYWORD = TokenSet.create(
        VRLElementTypes.IF,
        VRLElementTypes.ELSE,
        VRLElementTypes.FOR,
        VRLElementTypes.WHILE,
        VRLElementTypes.LOOP,
        VRLElementTypes.BREAK,
        VRLElementTypes.CONTINUE,
        VRLElementTypes.RETURN,
        VRLElementTypes.ABORT,
        VRLElementTypes.TRUE,
        VRLElementTypes.FALSE,
        VRLElementTypes.NULL,
        VRLElementTypes.IN,
    )

    val OPERATOR: TokenSet = TokenSet.create(
        VRLElementTypes.ASSIGN,
        VRLElementTypes.MERGE_ASSIGN,
        VRLElementTypes.NULL_COALESCE_ASSIGN,
        VRLElementTypes.EQ,
        VRLElementTypes.NE,
        VRLElementTypes.GE,
        VRLElementTypes.LE,
        VRLElementTypes.GT,
        VRLElementTypes.LT,
        VRLElementTypes.AND,
        VRLElementTypes.OR,
        VRLElementTypes.NULL_COALESCE,
        VRLElementTypes.NOT,
        VRLElementTypes.PLUS,
        VRLElementTypes.MINUS,
        VRLElementTypes.STAR,
        VRLElementTypes.SLASH,
        VRLElementTypes.PERCENT,
        VRLElementTypes.PIPE,
    )
}
