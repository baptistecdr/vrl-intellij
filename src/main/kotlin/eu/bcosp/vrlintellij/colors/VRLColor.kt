package eu.bcosp.vrlintellij.colors

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as Default

enum class VRLColor(humanName: String, val default: TextAttributesKey) {
    LINE_COMMENT("Comments//Line Comment", Default.LINE_COMMENT),

    IDENTIFIER("Comments//Identifier", Default.IDENTIFIER),

    KEYWORD("Keyword", Default.KEYWORD),
    NUMBER("Number", Default.NUMBER),

    STRING("String//String Literal", Default.STRING),
    ILLEGAL("String//Illegal character", Default.INVALID_STRING_ESCAPE),

    ARROW("Braces and Operators//Arrow", Default.PARENTHESES),
    PARENTHESES("Braces and Operators//Parenthesis", Default.PARENTHESES),
    BRACES("Braces and Operators//Braces", Default.BRACES),
    BRACKETS("Braces and Operators//Brackets", Default.BRACKETS),
    COLON("Braces and Operators//Colon", Default.SEMICOLON),
    SEMICOLON("Braces and Operators//Semicolon", Default.SEMICOLON),
    COMMA("Braces and Operators//Comma", Default.COMMA),
    DOT("Braces and Operators//Dot", Default.DOT),
    OPERATOR("Braces and Operators//Operator", Default.OPERATION_SIGN),

    FUNCTION_CALL("Functions//Function call", Default.FUNCTION_CALL),
    NAMED_ARGUMENTS("Named argument", Default.INSTANCE_FIELD),
    ;

    val textAttributesKey = TextAttributesKey.createTextAttributesKey("eu.bcosp.vrlintellij.$name", default)
    val attributesDescriptor = AttributesDescriptor(humanName, textAttributesKey)
}
