package eu.bcosp.vrlintellij.matching

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import eu.bcosp.vrlintellij.psi.VRLElementTypes

class VRLBraceMatcher : PairedBraceMatcher {
    private val pairs = arrayOf(
        BracePair(VRLElementTypes.LPAREN, VRLElementTypes.RPAREN, false),
        BracePair(VRLElementTypes.LBRACE, VRLElementTypes.RBRACE, true),
        BracePair(VRLElementTypes.LBRACKET, VRLElementTypes.RBRACKET, false),
    )

    override fun getPairs(): Array<BracePair> = pairs

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int = openingBraceOffset
}
