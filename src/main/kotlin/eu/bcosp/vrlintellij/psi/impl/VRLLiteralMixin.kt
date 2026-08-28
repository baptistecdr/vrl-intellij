package eu.bcosp.vrlintellij.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafPsiElement
import eu.bcosp.vrlintellij.injection.VRLRawLiteralEscaper
import eu.bcosp.vrlintellij.injection.VRLStringLiteralEscaper
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLLiteral

/**
 * Only string-shaped literals (`"..."`, `s'...'`, `r'...'`) are valid injection hosts - numbers,
 * booleans, `null`, and timestamps have no "content" a foreign language could occupy.
 */
abstract class VRLLiteralMixin(node: ASTNode) : ASTWrapperPsiElement(node), VRLLiteral, PsiLanguageInjectionHost {

    private val leafType get() = firstChild?.node?.elementType

    override fun isValidHost(): Boolean =
        leafType == VRLElementTypes.STRING || leafType == VRLElementTypes.RAW_STRING || leafType == VRLElementTypes.REGEX

    override fun updateText(text: String): PsiLanguageInjectionHost {
        (firstChild as? LeafPsiElement)?.replaceWithText(text)
        return this
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> =
        if (leafType == VRLElementTypes.STRING) VRLStringLiteralEscaper(this) else VRLRawLiteralEscaper(this)
}
