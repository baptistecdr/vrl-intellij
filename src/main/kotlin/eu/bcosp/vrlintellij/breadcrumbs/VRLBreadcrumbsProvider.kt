package eu.bcosp.vrlintellij.breadcrumbs

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import eu.bcosp.vrlintellij.VRL
import eu.bcosp.vrlintellij.psi.VRLClosureExpr
import eu.bcosp.vrlintellij.psi.VRLIfExpr

/**
 * Shows `if`/`else if` conditions and closure parameter lists in the editor's breadcrumb bar -
 * VRL's only two constructs that meaningfully nest a caret several levels deep (a chain of
 * `else if`s, or a closure body inside `for_each`/`map`/etc), so they're what's worth surfacing
 * as a trail rather than every expression on the path to the root.
 */
class VRLBreadcrumbsProvider : BreadcrumbsProvider {

    override fun getLanguages(): Array<Language> = arrayOf(VRL)

    override fun acceptElement(element: PsiElement): Boolean = element is VRLIfExpr || element is VRLClosureExpr

    override fun getElementInfo(element: PsiElement): String = when (element) {
        is VRLIfExpr -> "if ${truncate(element.expression.text)}"
        is VRLClosureExpr -> "|${element.closureParams?.text.orEmpty()}|"
        else -> truncate(element.text)
    }

    private fun truncate(text: String, maxLength: Int = 30): String {
        val collapsed = text.replace(Regex("\\s+"), " ").trim()
        return if (collapsed.length <= maxLength) collapsed else collapsed.take(maxLength - 1) + "…"
    }
}
