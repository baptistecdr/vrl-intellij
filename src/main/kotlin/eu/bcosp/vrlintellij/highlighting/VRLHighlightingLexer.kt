package eu.bcosp.vrlintellij.highlighting

import com.intellij.lexer.LayeredLexer
import eu.bcosp.vrlintellij.grammars.VRLLexerAdapter
import eu.bcosp.vrlintellij.highlighting.template.VRLStringTemplateLexerAdapter
import eu.bcosp.vrlintellij.psi.VRLElementTypes

class VRLHighlightingLexer: LayeredLexer(VRLLexerAdapter()) {
    init {
        registerLayer(VRLStringTemplateLexerAdapter(), VRLElementTypes.STRING)
    }
}
