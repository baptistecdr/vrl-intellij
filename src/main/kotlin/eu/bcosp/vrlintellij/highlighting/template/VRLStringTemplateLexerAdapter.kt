package eu.bcosp.vrlintellij.highlighting.template

import com.intellij.lexer.FlexAdapter

class VRLStringTemplateLexerAdapter : FlexAdapter(VRLStringTemplateLexer(null))
