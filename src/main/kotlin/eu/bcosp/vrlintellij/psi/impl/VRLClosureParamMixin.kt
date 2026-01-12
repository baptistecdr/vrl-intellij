package eu.bcosp.vrlintellij.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.impl.source.tree.LeafPsiElement
import eu.bcosp.vrlintellij.psi.VRLClosureParam

abstract class VRLClosureParamMixin(node: ASTNode) : ASTWrapperPsiElement(node), VRLClosureParam, PsiNameIdentifierOwner {

    override fun getName(): String? = identifier.text

    override fun setName(name: String): PsiElement {
        (identifier as? LeafPsiElement)?.replaceWithText(name)
        return this
    }

    override fun getNameIdentifier(): PsiElement = identifier
}
