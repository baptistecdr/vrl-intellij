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

    /**
     * Every bare assignment target in [file] whose value is read somewhere - the set an unused
     * variable is defined by *not* being in.
     *
     * Shared by [eu.bcosp.vrlintellij.inspections.VRLUnusedVariableInspection] and
     * [eu.bcosp.vrlintellij.intentions.VRLReplaceErrorDestructuringWithRaiseIntention], which ask
     * the same question about the same model and would drift apart if each walked the tree itself.
     */
    fun readDeclarations(file: PsiFile): Set<PsiElement> {
        val read = mutableSetOf<PsiElement>()
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType == VRLElementTypes.PRIMARY_EXPR) {
                    val primaryExpr = element as VRLPrimaryExpr
                    val identifier = primaryExpr.identifier
                    if (identifier != null && !isBareAssignmentTarget(primaryExpr)) {
                        resolve(identifier)?.let { read.add(it) }
                    }
                }
                super.visitElement(element)
            }
        })
        return read
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
    fun isBareAssignmentTarget(primaryExpr: PsiElement): Boolean =
        assignmentTargetKind(primaryExpr) != AssignmentTargetKind.NONE

    /** Distinguishes the two assignment-target shapes [isBareAssignmentTarget] treats alike -
     * callers that need to react differently to a plain `x = ...` versus a `value, err = ...`
     * target (e.g. [eu.bcosp.vrlintellij.inspections.VRLUnusedVariableInspection]'s quick fix:
     * `_` is only a legal replacement for a [MULTI] target, since VRL rejects a bare `_ = ...` as
     * a no-op assignment) use this instead of re-deriving the same tree walk. */
    fun assignmentTargetKind(primaryExpr: PsiElement): AssignmentTargetKind {
        var current = primaryExpr.node ?: return AssignmentTargetKind.NONE
        var parent = current.treeParent
        while (parent != null) {
            if (parent.elementType == VRLElementTypes.ASSIGNMENT_EXPR) {
                if (firstSignificantChild(parent) !== current) return AssignmentTargetKind.NONE
                val assignment = parent.psi as? VRLAssignmentExpr ?: return AssignmentTargetKind.NONE
                return if (assignment.assignmentExpr != null) AssignmentTargetKind.SINGLE else AssignmentTargetKind.NONE
            }
            if (parent.elementType == VRLElementTypes.MULTI_ASSIGNMENT_EXPR) {
                val targets = significantChildren(parent)
                val index = targets.indexOf(current)
                val isTarget = index == 0 || (index == 2 && targets.getOrNull(1)?.elementType == VRLElementTypes.COMMA)
                return if (isTarget) AssignmentTargetKind.MULTI else AssignmentTargetKind.NONE
            }
            if (onlySignificantChild(parent) !== current) return AssignmentTargetKind.NONE
            current = parent
            parent = current.treeParent
        }
        return AssignmentTargetKind.NONE
    }

    enum class AssignmentTargetKind { NONE, SINGLE, MULTI }

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
