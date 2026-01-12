package eu.bcosp.vrlintellij.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.tree.LeafPsiElement
import eu.bcosp.vrlintellij.psi.VRLPrimaryExpr
import eu.bcosp.vrlintellij.references.VRLVariableReference
import eu.bcosp.vrlintellij.references.VRLVariableResolver

/**
 * A [com.intellij.psi.PsiReferenceContributor] registered for [VRLPrimaryExpr] was tried first,
 * but it turned out to be invisible to the platform's real reference-lookup paths
 * (`PsiReferenceService`, plain `.getReferences()`, and everything Find Usages/Go to Declaration
 * actually call) in this project's setup — only the low-level `ReferenceProvidersRegistry` bypass
 * ever found it. Overriding `getReferences()` directly on the generated PSI class sidesteps that
 * indirection entirely, since every caller ends up invoking this method with no registry involved.
 *
 * [PsiNameIdentifierOwner] is implemented so this element can act as a rename target: at a bare
 * assignment (`x = ...`) [identifier] is this element's sole child, so its range never differs
 * from the identifier's own - `getNameIdentifier()` and the default navigation offset line up
 * for free.
 */
abstract class VRLPrimaryExprMixin(node: ASTNode) : ASTWrapperPsiElement(node), VRLPrimaryExpr, PsiNameIdentifierOwner {

    override fun getReference(): PsiReference? = references.firstOrNull()

    override fun getReferences(): Array<PsiReference> {
        val id = identifier ?: return PsiReference.EMPTY_ARRAY
        if (VRLVariableResolver.isBareAssignmentTarget(this)) return PsiReference.EMPTY_ARRAY
        return arrayOf(VRLVariableReference(this, id))
    }

    override fun getName(): String? = identifier?.text

    override fun setName(name: String): PsiElement {
        (identifier as? LeafPsiElement)?.replaceWithText(name)
        return this
    }

    override fun getNameIdentifier(): PsiElement? = identifier
}
