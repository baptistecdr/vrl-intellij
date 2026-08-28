package eu.bcosp.vrlintellij.references

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.PsiTreeUtil
import eu.bcosp.vrlintellij.psi.VRLAssignmentExpr
import eu.bcosp.vrlintellij.psi.VRLClosureExpr
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLPrimaryExpr
import eu.bcosp.vrlintellij.psi.isWhitespaceOrComment

/**
 * VRL has no `let`/declaration keyword: `x = ...` both declares and reassigns `x`, and closures
 * bind their own params (`|x| { ... }`). This resolves a bare-identifier usage to the nearest
 * enclosing closure param, or otherwise to the closest *preceding* bare assignment of the same
 * name anywhere in the file. It intentionally ignores block/branch scoping (that would require a
 * full symbol table); "nearest preceding assignment wins" is a reasonable approximation for the
 * short, mostly-linear scripts VRL is used for.
 */
object VRLVariableResolver {

    fun resolve(usage: PsiElement): PsiElement? {
        val name = usage.text
        findClosureParam(usage, name)?.let { return it }
        return findNearestPrecedingAssignment(usage.containingFile, name, usage.textRange.startOffset)
    }

    /** Every variable name visible from `fromOffset`: enclosing closure params plus every
     * preceding bare assignment in the file, closure params first (innermost scope first). */
    fun visibleVariableNames(fromOffset: Int, contextElement: PsiElement): List<String> {
        val names = LinkedHashSet<String>()
        var closure = PsiTreeUtil.getParentOfType(contextElement, VRLClosureExpr::class.java)
        while (closure != null) {
            closure.closureParams?.closureParamList?.forEach { names.add(it.text) }
            closure = PsiTreeUtil.getParentOfType(closure, VRLClosureExpr::class.java)
        }
        contextElement.containingFile.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType == VRLElementTypes.PRIMARY_EXPR) {
                    val identifier = (element as? VRLPrimaryExpr)?.identifier
                    if (identifier != null && identifier.textRange.startOffset < fromOffset &&
                        isBareAssignmentTarget(element)
                    ) {
                        names.add(identifier.text)
                    }
                }
                super.visitElement(element)
            }
        })
        return names.toList()
    }

    /** True if `primaryExpr` sits in assignment-target position: either the sole target of a
     * plain `x = ...` assignment, or either target of an error-destructuring
     * `value, err = fallible_call()` assignment. */
    fun isBareAssignmentTarget(primaryExpr: PsiElement): Boolean {
        var current = primaryExpr.node ?: return false
        var parent = current.treeParent
        while (parent != null) {
            if (parent.elementType == VRLElementTypes.ASSIGNMENT_EXPR) {
                if (firstSignificantChild(parent) !== current) return false
                val assignment = parent.psi as? VRLAssignmentExpr ?: return false
                return assignment.assignmentExpr != null
            }
            if (parent.elementType == VRLElementTypes.MULTI_ASSIGNMENT_EXPR) {
                val targets = significantChildren(parent)
                val index = targets.indexOf(current)
                return index == 0 || (index == 2 && targets.getOrNull(1)?.elementType == VRLElementTypes.COMMA)
            }
            if (onlySignificantChild(parent) !== current) return false
            current = parent
            parent = current.treeParent
        }
        return false
    }

    private fun findClosureParam(usage: PsiElement, name: String): PsiElement? {
        var closure = PsiTreeUtil.getParentOfType(usage, VRLClosureExpr::class.java)
        while (closure != null) {
            val match = closure.closureParams?.closureParamList?.firstOrNull { it.text == name }
            if (match != null) return match
            closure = PsiTreeUtil.getParentOfType(closure, VRLClosureExpr::class.java)
        }
        return null
    }

    private fun findNearestPrecedingAssignment(file: PsiFile, name: String, beforeOffset: Int): PsiElement? {
        var best: PsiElement? = null
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType == VRLElementTypes.PRIMARY_EXPR) {
                    val identifier = (element as? VRLPrimaryExpr)?.identifier
                    val candidate = best
                    if (identifier != null && identifier.text == name &&
                        identifier.textRange.startOffset < beforeOffset &&
                        (candidate == null || identifier.textRange.startOffset > candidate.textRange.startOffset) &&
                        isBareAssignmentTarget(element)
                    ) {
                        best = element
                    }
                }
                super.visitElement(element)
            }
        })
        return best
    }

    private fun onlySignificantChild(node: ASTNode): ASTNode? {
        val children = node.getChildren(null).filterNot { isWhitespaceOrComment(it.elementType) }
        return children.singleOrNull()
    }

    private fun firstSignificantChild(node: ASTNode): ASTNode? {
        return node.getChildren(null).firstOrNull { !isWhitespaceOrComment(it.elementType) }
    }

    private fun significantChildren(node: ASTNode): List<ASTNode> {
        return node.getChildren(null).filterNot { isWhitespaceOrComment(it.elementType) }
    }
}
