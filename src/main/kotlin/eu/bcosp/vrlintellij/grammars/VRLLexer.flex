package eu.bcosp.vrlintellij.grammars;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;

import static eu.bcosp.vrlintellij.psi.VRLElementTypes.*;

%%

%class VRLLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

%state STRING_STATE
%state RAW_STRING_STATE
%state REGEX_STATE

WHITE_SPACE=[\s]+
LINE_COMMENT="#"[^\r\n]*
FLOAT=[0-9][0-9_]*\.[0-9][0-9_]*([eE][+-]?[0-9]+)?
INTEGER=[0-9][0-9_]*
IDENTIFIER=[a-zA-Z_][a-zA-Z0-9_]*
STRING_LITERAL=\"([^\"\\\r\n]|\\.|\\\r?\n[ \t]*)*\"
RAW_STRING_LITERAL=s\'([^\']|\'\')*\'
REGEX_LITERAL=r\'([^\']|\'\')*\'
TIMESTAMP_LITERAL=t\'[^\']*\'

%%

<YYINITIAL> {
    {WHITE_SPACE}          { return TokenType.WHITE_SPACE; }
    {LINE_COMMENT}         { return COMMENT; }

    // Keywords
    "if"                   { return IF; }
    "else"                 { return ELSE; }
    "for"                  { return FOR; }
    "while"                { return WHILE; }
    "loop"                 { return LOOP; }
    "break"                { return BREAK; }
    "continue"             { return CONTINUE; }
    "return"               { return RETURN; }
    "abort"                { return ABORT; }
    "true"                 { return TRUE; }
    "false"                { return FALSE; }
    "null"                 { return NULL; }

    // Operators
    "="                    { return ASSIGN; }
    "|="                   { return MERGE_ASSIGN; }
    "??="                  { return NULL_COALESCE_ASSIGN; }
    "=="                   { return EQ; }
    "!="                   { return NE; }
    ">="                   { return GE; }
    "<="                   { return LE; }
    ">"                    { return GT; }
    "<"                    { return LT; }
    "&&"                   { return AND; }
    "||"                   { return OR; }
    "??"                   { return NULL_COALESCE; }
    "!"                    { return NOT; }
    "+"                    { return PLUS; }
    "-"                    { return MINUS; }
    "*"                    { return STAR; }
    "/"                    { return SLASH; }
    "%"                    { return PERCENT; }
    "|"                    { return PIPE; }

    // Punctuation
    "("                    { return LPAREN; }
    ")"                    { return RPAREN; }
    "{"                    { return LBRACE; }
    "}"                    { return RBRACE; }
    "["                    { return LBRACKET; }
    "]"                    { return RBRACKET; }
    ","                    { return COMMA; }
    ":"                    { return COLON; }
    ";"                    { return SEMICOLON; }
    "."                    { return DOT; }
    "?"                    { return QUESTION; }
    "->"                   { return ARROW; }

    // Literals
    {TIMESTAMP_LITERAL}    { return TIMESTAMP; }
    {RAW_STRING_LITERAL}   { return RAW_STRING; }
    {REGEX_LITERAL}        { return REGEX; }
    {STRING_LITERAL}       { return STRING; }
    {FLOAT}                { return FLOAT_LITERAL; }
    {INTEGER}              { return INTEGER_LITERAL; }

    // Identifiers (including paths)
    "."                    { return DOT; }
    {IDENTIFIER}(\?=\(|\!) { return FUNCTION_CALL; }
    {IDENTIFIER}           { return IDENTIFIER; }
}

[^]                        { return TokenType.BAD_CHARACTER; }
