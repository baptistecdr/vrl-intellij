package eu.bcosp.vrlintellij.annotators

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import eu.bcosp.vrlintellij.colors.VRLColor
import eu.bcosp.vrlintellij.psi.VRLArgument
import eu.bcosp.vrlintellij.psi.VRLElementTypes

class VRLNamedArgumentAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is VRLArgument) {
            return
        }

        val name = element.getIdentifier() ?: return

        element.node.findChildByType(VRLElementTypes.COLON) ?: return

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(name.textRange)
            .textAttributes(VRLColor.NAMED_ARGUMENTS.textAttributesKey)
            .create()
    }
}
