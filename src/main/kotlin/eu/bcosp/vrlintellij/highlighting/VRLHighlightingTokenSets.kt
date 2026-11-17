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
    // PATH_FIELD (`@timestamp`) is an identifier-shaped token too; in path position
    // VRLPathAnnotator recolors it as a field like any other.
    val IDENTIFIER = TokenSet.create(VRLElementTypes.IDENTIFIER, VRLElementTypes.PATH_FIELD)

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
        VRLElementTypes.RAW_STRING,
        TEMPLATE_START,
        TEMPLATE_END,
    )

    val TEMPLATE_VARIABLE_LITERAL = TokenSet.create(TEMPLATE_VARIABLE)

    val REGEX_LITERAL = TokenSet.create(VRLElementTypes.REGEX)

    val TIMESTAMP_LITERAL = TokenSet.create(VRLElementTypes.TIMESTAMP)

    val NUMBER_LITERAL = TokenSet.create(
        VRLElementTypes.INTEGER_LITERAL,
        VRLElementTypes.FLOAT_LITERAL,
    )

    val KEYWORD = TokenSet.create(
        VRLElementTypes.IF,
        VRLElementTypes.ELSE,
        VRLElementTypes.BREAK,
        VRLElementTypes.RETURN,
        VRLElementTypes.ABORT,
        // `for`, `while`, `string`, ...: vrl reports them as "reserved keyword" (E205) when used
        // as a variable. In path position (`.for`) VRLPathAnnotator recolors them as a field.
        VRLElementTypes.RESERVED_IDENTIFIER,
    )

    val CONSTANT = TokenSet.create(
        VRLElementTypes.TRUE,
        VRLElementTypes.FALSE,
        VRLElementTypes.NULL,
    )

    val OPERATOR: TokenSet = TokenSet.create(
        VRLElementTypes.ASSIGN,
        VRLElementTypes.MERGE_ASSIGN,
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
        VRLElementTypes.QUESTION,
    )
}
