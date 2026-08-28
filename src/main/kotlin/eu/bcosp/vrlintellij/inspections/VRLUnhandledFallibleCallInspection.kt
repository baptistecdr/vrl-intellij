package eu.bcosp.vrlintellij.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import eu.bcosp.vrlintellij.functions.allFunctions
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLPostfixExpr
import eu.bcosp.vrlintellij.psi.VRLPrimaryExpr
import eu.bcosp.vrlintellij.psi.isWhitespaceOrComment

/**
 * VRL's compiler rejects any fallible call whose error isn't handled (its own compile errors 100
 * "Unhandled root runtime error" / 103 "Unhandled fallible assignment") - it must be handled with
 * the `!` raise suffix, a `value, err = ...` destructuring assignment, or a `??` fallback. This
 * mirrors that check using the fallibility already recorded per-function in [allFunctions].
 */
class VRLUnhandledFallibleCallInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType != VRLElementTypes.FUNCTION_CALL) return
                val name = element.text
                val function = allFunctions[name] ?: return
                if (!function.isFallible) return
                if (isHandled(element)) return

                holder.registerProblem(
                    element,
                    "Unhandled fallible function call '$name'. VRL requires the error to be " +
                        "handled with '!', a 'value, err = ...' assignment, or '??' " +
                        "(compiler errors 100/103).",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    AddRaiseFlagQuickFix,
                )
            }
        }
    }

    private fun isHandled(functionCall: PsiElement): Boolean {
        val primaryExpr = functionCall.parent as? VRLPrimaryExpr ?: return false
        val postfixExpr = primaryExpr.parent as? VRLPostfixExpr ?: return false
        if (postfixExpr.postfixSuffixList.firstOrNull()?.callSuffix?.raiseFlag != null) return true

        var current = postfixExpr.node ?: return false
        var parent = current.treeParent
        while (parent != null) {
            if (parent.elementType == VRLElementTypes.NULL_COALESCE_EXPR &&
                parent.getChildren(null).any { it.elementType == VRLElementTypes.NULL_COALESCE }
            ) {
                return true
            }
            if (parent.elementType == VRLElementTypes.ASSIGNMENT_EXPR) {
                return parent.treeParent?.elementType == VRLElementTypes.MULTI_ASSIGNMENT_EXPR
            }
            if (onlySignificantChild(parent) !== current) return false
            current = parent
            parent = current.treeParent
        }
        return false
    }

    private fun onlySignificantChild(node: ASTNode): ASTNode? {
        val children = node.getChildren(null).filterNot { isWhitespaceOrComment(it.elementType) }
        return children.singleOrNull()
    }

    private object AddRaiseFlagQuickFix : LocalQuickFix {
        override fun getFamilyName(): String = "Add '!' to raise on error"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement
            val document = PsiDocumentManager.getInstance(project).getDocument(element.containingFile) ?: return
            document.insertString(element.textRange.endOffset, "!")
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }
}
