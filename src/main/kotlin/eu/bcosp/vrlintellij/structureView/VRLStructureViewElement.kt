package eu.bcosp.vrlintellij.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import eu.bcosp.vrlintellij.psi.VRLAbortExpr
import eu.bcosp.vrlintellij.psi.VRLAssignmentExpr
import eu.bcosp.vrlintellij.psi.VRLBlockExpr
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLFile
import eu.bcosp.vrlintellij.psi.VRLIfExpr
import eu.bcosp.vrlintellij.psi.VRLReturnExpr
import eu.bcosp.vrlintellij.psi.VRLStatement
import eu.bcosp.vrlintellij.psi.collapsePassThroughWrappers

/**
 * A node's children are the statements of every [VRLBlockExpr] reachable from it without passing
 * through another statement first (an if's own block(s), or a closure body) - this
 * naturally mirrors real nesting: a block found one level down contributes its statements as this
 * node's children, while a block found two levels down (inside one of those) becomes a grandchild
 * on the next recursive call instead of being flattened in here.
 */
class VRLStructureViewElement(element: PsiElement) : PsiTreeElementBase<PsiElement>(element) {

    override fun getPresentableText(): String = when (val element = element) {
        is VRLFile -> element.name
        is VRLStatement -> describe(element)
        else -> truncate(element?.text.orEmpty())
    }

    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val statements = when (val element = element) {
            is VRLFile -> PsiTreeUtil.getChildrenOfType(element, VRLStatement::class.java)?.toList().orEmpty()
            is VRLStatement -> topLevelBlocksIn(element).flatMap { it.statementList }
            else -> emptyList()
        }
        return statements.map { VRLStructureViewElement(it) }
    }

    private fun topLevelBlocksIn(element: PsiElement): List<VRLBlockExpr> {
        val result = mutableListOf<VRLBlockExpr>()
        fun visit(node: PsiElement) {
            for (child in node.children) {
                if (child is VRLBlockExpr) result.add(child) else visit(child)
            }
        }
        visit(element)
        return result
    }

    private fun describe(statement: VRLStatement): String {
        statement.multiAssignmentExpr?.let { multi ->
            return multi.orExprList.joinToString(", ") { truncate(it.text) }
        }
        val assignment = statement.expression?.assignmentExpr ?: return truncate(statement.text)
        if (assignment.assignmentExpr != null) {
            return "${truncate(assignment.orExpr.text)} ${operatorSymbol(assignment)}"
        }
        return describeConstruct(assignment.orExpr) ?: truncate(assignment.text)
    }

    private fun operatorSymbol(assignment: VRLAssignmentExpr): String = when {
        assignment.node.findChildByType(VRLElementTypes.MERGE_ASSIGN) != null -> "|="
        else -> "="
    }

    // Descends the precedence-wrapper chain (same collapse used by the formatter) to find the
    // actual control-flow construct a bare (non-assignment) statement's expression is, if any.
    private fun describeConstruct(orExpr: PsiElement): String? {
        val primary = collapsePassThroughWrappers(orExpr.node).psi
        return when (primary) {
            is VRLIfExpr -> textBefore(primary, primary.blockExprList.first())
            is VRLAbortExpr, is VRLReturnExpr -> truncate(primary.text)
            else -> null
        }
    }

    private fun textBefore(element: PsiElement, boundary: PsiElement): String =
        truncate(element.text.substring(0, boundary.textRange.startOffset - element.textRange.startOffset))

    private fun truncate(text: String, maxLength: Int = 60): String {
        val collapsed = text.replace(Regex("\\s+"), " ").trim()
        return if (collapsed.length <= maxLength) collapsed else collapsed.take(maxLength - 1) + "…"
    }
}
