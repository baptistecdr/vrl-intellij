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

                val suggestion = closestFunctionName(name)
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

    private fun closestFunctionName(name: String): String? {
        val maxDistance = if (name.length <= 4) 1 else 2
        return allFunctions.keys
            .map { it to levenshtein(name, it) }
            .filter { it.second <= maxDistance }
            .minByOrNull { it.second }
            ?.first
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        return dp[a.length][b.length]
    }

    private class RenameToQuickFix(private val suggestion: String) : LocalQuickFix {
        override fun getFamilyName(): String = "Change to '$suggestion'"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            (descriptor.psiElement as? LeafPsiElement)?.replaceWithText(suggestion)
        }
    }
}
