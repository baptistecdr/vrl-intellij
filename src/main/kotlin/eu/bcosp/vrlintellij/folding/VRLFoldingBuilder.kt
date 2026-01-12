package eu.bcosp.vrlintellij.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import eu.bcosp.vrlintellij.psi.VRLElementTypes

class VRLFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        root.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                val type = element.node?.elementType
                if (type == VRLElementTypes.BLOCK_EXPR || type == VRLElementTypes.OBJECT_EXPR || type == VRLElementTypes.ARRAY_EXPR) {
                    val range = element.textRange
                    if (range.length > 1 && document.getLineNumber(range.startOffset) != document.getLineNumber(range.endOffset)) {
                        descriptors.add(FoldingDescriptor(element.node, range))
                    }
                }
                super.visitElement(element)
            }
        })
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return if (node.elementType == VRLElementTypes.ARRAY_EXPR) "[...]" else "{...}"
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
