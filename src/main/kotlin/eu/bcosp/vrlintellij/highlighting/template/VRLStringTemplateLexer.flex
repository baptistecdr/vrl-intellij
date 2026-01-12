package eu.bcosp.vrlintellij.highlighting.template;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;

import static eu.bcosp.vrlintellij.psi.VRLElementTypes.STRING;
import static eu.bcosp.vrlintellij.highlighting.VRLTemplateElementTypes.TEMPLATE_START;
import static eu.bcosp.vrlintellij.highlighting.VRLTemplateElementTypes.TEMPLATE_END;
import static eu.bcosp.vrlintellij.highlighting.VRLTemplateElementTypes.TEMPLATE_VARIABLE;

%%

%class VRLStringTemplateLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

%state TEMPLATE

%%

<YYINITIAL> {
    "\\{"                    { return STRING; }
    "\\\\"                   { return STRING; }
    "{{"                     { yybegin(TEMPLATE); return TEMPLATE_START; }
    [^\\{]+                  { return STRING; }
    .                        { return STRING; }
}

<TEMPLATE> {
    [ \t]+                   { return TokenType.WHITE_SPACE; }
    "}}"                     { yybegin(YYINITIAL); return TEMPLATE_END; }
    [a-zA-Z_][a-zA-Z0-9_]*   { return TEMPLATE_VARIABLE; }
    "}"                      { return STRING; }
    [^\}]                    { return STRING; }
}
