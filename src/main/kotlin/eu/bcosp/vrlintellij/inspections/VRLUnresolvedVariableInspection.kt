package eu.bcosp.vrlintellij.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLPrimaryExpr
import eu.bcosp.vrlintellij.references.VRLVariableResolver

/**
 * Flags a bare variable read (`x`) that [VRLVariableResolver] can't resolve to anything - no
 * enclosing closure param and no preceding bare assignment of that name anywhere in the file -
 * almost always a typo'd variable name. The mirror image of [VRLUnusedVariableInspection], which
 * catches the same class of typo on the write side instead.
 */
class VRLUnresolvedVariableInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType != VRLElementTypes.PRIMARY_EXPR) return
                val primaryExpr = element as VRLPrimaryExpr
                val identifier = primaryExpr.identifier ?: return
                if (VRLVariableResolver.isBareAssignmentTarget(primaryExpr)) return
                if (VRLVariableResolver.resolve(identifier) != null) return

                val name = identifier.text
                val visible = VRLVariableResolver.visibleVariableNames(identifier.textRange.startOffset, primaryExpr)
                val suggestion = closestMatch(name, visible)
                val fixes = if (suggestion != null) arrayOf<LocalQuickFix>(RenameToQuickFix(suggestion)) else emptyArray()
                val message = if (suggestion != null) {
                    "Unresolved variable '$name'. Did you mean '$suggestion'?"
                } else {
                    "Unresolved variable '$name'"
                }
                holder.registerProblem(identifier, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, *fixes)
            }
        }
    }

    private class RenameToQuickFix(private val suggestion: String) : LocalQuickFix {
        override fun getFamilyName(): String = "Change to '$suggestion'"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            (descriptor.psiElement as? LeafPsiElement)?.replaceWithText(suggestion)
        }
    }
}
