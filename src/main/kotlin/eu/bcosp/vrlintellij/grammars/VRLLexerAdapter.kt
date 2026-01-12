package eu.bcosp.vrlintellij.grammars

import com.intellij.lexer.FlexAdapter

class VRLLexerAdapter: FlexAdapter(VRLLexer(null))
