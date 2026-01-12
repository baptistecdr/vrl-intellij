package eu.bcosp.vrlintellij.references

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.source.tree.LeafPsiElement
import eu.bcosp.vrlintellij.psi.VRLPrimaryExpr

/**
 * Anchored on the [VRLPrimaryExpr] composite rather than the identifier leaf it wraps.
 * [com.intellij.psi.impl.source.tree.LeafPsiElement] implements `HintedReferenceHost` and defers
 * to its parent when the platform's real reference lookup (`PsiReferenceService`, which Find
 * Usages and Go to Declaration actually use) asks a leaf for references with hints — so a
 * reference registered directly on the leaf is invisible to those code paths even though it's
 * reachable via the lower-level `ReferenceProvidersRegistry` bypass. Anchoring on the composite
 * parent sidesteps that.
 */
class VRLVariableReference(primaryExpr: VRLPrimaryExpr, private val identifier: PsiElement) :
    PsiReferenceBase<PsiElement>(primaryExpr, identifier.textRange.shiftLeft(primaryExpr.textRange.startOffset)) {

    override fun resolve(): PsiElement? = VRLVariableResolver.resolve(identifier)

    override fun getVariants(): Array<Any> = emptyArray()

    /**
     * The default [PsiReferenceBase.handleElementRename] looks up an [com.intellij.psi.ElementManipulator]
     * for the anchor element ([VRLPrimaryExpr]) to do a generic range replacement - none is
     * registered for it, so this replaces the wrapped identifier leaf's text directly instead,
     * the same way [eu.bcosp.vrlintellij.inspections.VRLUnknownFunctionInspection]'s quick fix does.
     */
    override fun handleElementRename(newElementName: String): PsiElement {
        (identifier as? LeafPsiElement)?.replaceWithText(newElementName)
        return element
    }
}
