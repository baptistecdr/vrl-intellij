package eu.bcosp.vrlintellij.quotes

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import eu.bcosp.vrlintellij.psi.VRLElementTypes

class VRLQuoteHandler : SimpleTokenSetQuoteHandler(VRLElementTypes.STRING)
