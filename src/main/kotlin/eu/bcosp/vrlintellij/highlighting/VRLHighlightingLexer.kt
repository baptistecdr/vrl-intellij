package eu.bcosp.vrlintellij.highlighting

import com.intellij.lexer.LayeredLexer
import eu.bcosp.vrlintellij.grammars.VRLLexerAdapter

class VRLHighlightingLexer: LayeredLexer(VRLLexerAdapter())
