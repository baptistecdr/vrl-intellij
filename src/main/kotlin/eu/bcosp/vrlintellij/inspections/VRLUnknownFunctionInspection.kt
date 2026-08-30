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
import eu.bcosp.vrlintellij.functions.allFunctions
import eu.bcosp.vrlintellij.psi.VRLElementTypes

class VRLUnknownFunctionInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType != VRLElementTypes.FUNCTION_CALL) return
                val name = element.text
                if (allFunctions.containsKey(name)) return

                val suggestion = closestMatch(name, allFunctions.keys)
                val fixes = if (suggestion != null) arrayOf<LocalQuickFix>(RenameToQuickFix(suggestion)) else emptyArray()
                val message = if (suggestion != null) {
                    "Unknown function '$name'. Did you mean '$suggestion'?"
                } else {
                    "Unknown function '$name'"
                }
                holder.registerProblem(element, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, *fixes)
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
