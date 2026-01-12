package eu.bcosp.vrlintellij.references

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import eu.bcosp.vrlintellij.psi.VRLElementTypes

/**
 * The generic word-index-based [ReferencesSearch] only calls `getReferences()` on the exact leaf
 * token it finds at each text occurrence — it never walks up to a wrapping composite. Our
 * variable references live on the enclosing [eu.bcosp.vrlintellij.psi.VRLPrimaryExpr] (see
 * [eu.bcosp.vrlintellij.psi.impl.VRLPrimaryExprMixin]), so the generic search misses them
 * entirely (Go to Declaration still works, since that path resolves from a known offset and does
 * walk up ancestors). This does the search directly instead of relying on that mechanism.
 */
class VRLReferencesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {

    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        val target = queryParameters.elementToSearch
        val isValidTarget = when (target.node?.elementType) {
            VRLElementTypes.PRIMARY_EXPR -> VRLVariableResolver.isBareAssignmentTarget(target)
            VRLElementTypes.CLOSURE_PARAM -> true
            else -> false
        }
        if (!isValidTarget) return

        val file = target.containingFile ?: return
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType == VRLElementTypes.PRIMARY_EXPR) {
                    val reference = element.reference
                    if (reference != null && reference.isReferenceTo(target)) {
                        if (!consumer.process(reference)) return
                    }
                }
                super.visitElement(element)
            }
        })
    }
}
