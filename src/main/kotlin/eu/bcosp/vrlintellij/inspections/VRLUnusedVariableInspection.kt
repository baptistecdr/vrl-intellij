package eu.bcosp.vrlintellij.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import eu.bcosp.vrlintellij.psi.VRLAssignmentExpr
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLPrimaryExpr
import eu.bcosp.vrlintellij.references.VRLVariableResolver
import eu.bcosp.vrlintellij.references.VRLVariableResolver.AssignmentTargetKind

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
        val usedDeclarations = VRLVariableResolver.readDeclarations(holder.file)
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType != VRLElementTypes.PRIMARY_EXPR) return
                val primaryExpr = element as VRLPrimaryExpr
                val identifier = primaryExpr.identifier ?: return
                if (identifier.text == "_") return
                val kind = VRLVariableResolver.assignmentTargetKind(primaryExpr)
                if (kind == AssignmentTargetKind.NONE) return
                if (primaryExpr in usedDeclarations) return

                // `_` is only a legal target in the `value, err = ...` destructuring form - VRL
                // rejects a bare `_ = ...` as a no-op assignment (compiler error 640), so a plain
                // `x = ...` is fixed by dropping the assignment instead, keeping the right-hand
                // side as a bare statement in case evaluating it matters (e.g. a fallible call).
                val fix = if (kind == AssignmentTargetKind.MULTI) RenameToUnderscoreQuickFix else RemoveUnusedAssignmentQuickFix
                holder.registerProblem(
                    identifier,
                    "Variable '${identifier.text}' is never used",
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                    fix,
                )
            }
        }
    }

    private object RenameToUnderscoreQuickFix : LocalQuickFix {
        override fun getFamilyName(): String = "Rename to '_'"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            (descriptor.psiElement as? LeafPsiElement)?.replaceWithText("_")
        }
    }

    private object RemoveUnusedAssignmentQuickFix : LocalQuickFix {
        override fun getFamilyName(): String = "Remove unused assignment"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val identifier = descriptor.psiElement
            val primaryExpr = identifier.parent as? VRLPrimaryExpr ?: return
            val assignmentExpr = PsiTreeUtil.getParentOfType(primaryExpr, VRLAssignmentExpr::class.java) ?: return
            val rhs = assignmentExpr.assignmentExpr ?: return
            val document = PsiDocumentManager.getInstance(project).getDocument(assignmentExpr.containingFile) ?: return
            document.replaceString(assignmentExpr.textRange.startOffset, assignmentExpr.textRange.endOffset, rhs.text)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }
}
