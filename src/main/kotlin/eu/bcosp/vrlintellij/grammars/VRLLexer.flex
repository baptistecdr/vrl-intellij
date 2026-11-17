package eu.bcosp.vrlintellij.grammars;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;

import static eu.bcosp.vrlintellij.psi.VRLElementTypes.*;
import static eu.bcosp.vrlintellij.psi.ElementTypesKt.NEWLINE;

%%

%class VRLLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

%{
    // vrl's lex.rs (Token::ident) reports these as "reserved keyword" (E205) when used as a
    // variable; they're still valid path fields (`.for`, `.string`), see VRL.bnf's field_name.
    private static final java.util.Set<String> RESERVED_IDENTIFIERS = java.util.Set.of(
        "array", "bool", "boolean", "continue", "do", "emit", "float", "for", "forall", "foreach",
        "all", "each", "any", "try", "undefined", "int", "integer", "iter", "object", "regex",
        "string", "traverse", "timestamp", "duration", "unless", "walk", "while", "loop"
    );

    private IElementType identifierType(CharSequence text) {
        // lex.rs: `@` is an identifier character, and an identifier containing one is a
        // PathField - only usable as a path segment (`.@timestamp`), never as a variable.
        if (text.toString().indexOf('@') >= 0) return PATH_FIELD;
        if (RESERVED_IDENTIFIERS.contains(text.toString())) return RESERVED_IDENTIFIER;
        return IDENTIFIER;
    }
%}

WHITE_SPACE=[\s]+
LINE_COMMENT="#"[^\r\n]*
// lex.rs's numeric_literal_or_identifier: an optional leading `-` belongs to the literal (there's
// no unary minus - `- 1` and `-x` are syntax errors), and there's no exponent part.
FLOAT=-?[0-9][0-9_]*\.[0-9][0-9_]*
INTEGER=-?[0-9][0-9_]*
IDENTIFIER=[a-zA-Z_@][a-zA-Z0-9_@]*
// lex.rs's numeric_literal_or_identifier: a digit run followed by an identifier character is one
// identifier, not a number then a name - `5xx = 1` and `.http.5xx` both compile. Never a function
// call (`5xx(1)` is E203), and a leading `-` keeps it a number (`-5xx` is `-5` then `xx`).
DIGIT_IDENTIFIER=[0-9][0-9_]*[a-zA-Z_@][a-zA-Z0-9_@]*
// lex.rs's string_literal runs to the next unescaped `"`, across newlines. The closing quote is
// optional so a freshly-typed opening quote still lexes as STRING (VRLQuoteHandler needs that).
STRING_LITERAL=\"([^\"\\]|\\[^])*\"?
RAW_STRING_LITERAL=s\'([^\'\\]|\\.)*\'
REGEX_LITERAL=r\'([^\'\\]|\\.)*\'
TIMESTAMP_LITERAL=t\'([^\'\\]|\\.)*\'

%%

<YYINITIAL> {
    // A whitespace run containing a newline is tagged distinctly from plain horizontal
    // whitespace: both stay auto-skipped everywhere by default (see
    // VRLParserDefinition.getWhitespaceTokens), but the grammar's <<newlineBefore>> predicate
    // can tell them apart via PsiBuilder.rawLookup to stop a value from continuing across a
    // line break, per VRL's "expressions can be separated by newline or semicolon" rule.
    {WHITE_SPACE}          { return yytext().toString().indexOf('\n') >= 0 ? NEWLINE : TokenType.WHITE_SPACE; }
    {LINE_COMMENT}         { return COMMENT; }

    // Keywords
    "if"                   { return IF; }
    "else"                 { return ELSE; }
    "break"                { return BREAK; }
    "return"               { return RETURN; }
    "abort"                { return ABORT; }
    "true"                 { return TRUE; }
    "false"                { return FALSE; }
    "null"                 { return NULL; }

    // Operators
    "="                    { return ASSIGN; }
    "|="                   { return MERGE_ASSIGN; }
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
    {DIGIT_IDENTIFIER}     { return identifierType(yytext()); }
    {FLOAT}                { return FLOAT_LITERAL; }
    {INTEGER}              { return INTEGER_LITERAL; }

    // Identifiers (including paths)
    {IDENTIFIER} / "!"?"(" { return FUNCTION_CALL; }
    {IDENTIFIER}           { return identifierType(yytext()); }
}

[^]                        { return TokenType.BAD_CHARACTER; }
