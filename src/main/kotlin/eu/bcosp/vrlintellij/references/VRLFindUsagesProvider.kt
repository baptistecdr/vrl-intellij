package eu.bcosp.vrlintellij.references

import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import eu.bcosp.vrlintellij.psi.VRLElementTypes

class VRLFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner? = null

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        val type = psiElement.node?.elementType
        return type == VRLElementTypes.PRIMARY_EXPR || type == VRLElementTypes.CLOSURE_PARAM
    }

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String = "variable"

    override fun getDescriptiveName(element: PsiElement): String = element.text

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String = element.text
}
