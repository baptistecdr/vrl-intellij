package eu.bcosp.vrlintellij.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLPrimaryExpr
import eu.bcosp.vrlintellij.references.VRLVariableResolver

/**
 * Flags a bare variable assignment (`x = ...`, or either target of `value, err = fallible_call()`)
 * whose value is never read afterward - almost always a typo'd variable name or dead computation.
 * `_` is exempt since it's the conventional "intentionally discarded" name (also what
 * [eu.bcosp.vrlintellij.postfix.VRLIfErrPostfixTemplate] itself generates for a bare fallible call).
 *
 * Reuses [VRLVariableResolver.resolve] - the same "nearest preceding bare assignment wins" model
 * Find Usages/Go to Declaration are built on - rather than re-deriving usage resolution here, so
 * this inherits that model's known block/branch-scoping approximation rather than disagreeing
 * with it.
 */
class VRLUnusedVariableInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val usedDeclarations = collectUsedDeclarations(holder.file)
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType != VRLElementTypes.PRIMARY_EXPR) return
                val primaryExpr = element as VRLPrimaryExpr
                val identifier = primaryExpr.identifier ?: return
                if (identifier.text == "_") return
                if (!VRLVariableResolver.isBareAssignmentTarget(primaryExpr)) return
                if (primaryExpr in usedDeclarations) return

                holder.registerProblem(
                    identifier,
                    "Variable '${identifier.text}' is never used",
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                    RenameToUnderscoreQuickFix,
                )
            }
        }
    }

    private fun collectUsedDeclarations(file: PsiFile): Set<PsiElement> {
        val used = mutableSetOf<PsiElement>()
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType == VRLElementTypes.PRIMARY_EXPR) {
                    val primaryExpr = element as VRLPrimaryExpr
                    val identifier = primaryExpr.identifier
                    if (identifier != null && !VRLVariableResolver.isBareAssignmentTarget(primaryExpr)) {
                        VRLVariableResolver.resolve(identifier)?.let { used.add(it) }
                    }
                }
                super.visitElement(element)
            }
        })
        return used
    }

    private object RenameToUnderscoreQuickFix : LocalQuickFix {
        override fun getFamilyName(): String = "Rename to '_'"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            (descriptor.psiElement as? LeafPsiElement)?.replaceWithText("_")
        }
    }
}
