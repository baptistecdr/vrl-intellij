package eu.bcosp.vrlintellij.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement

/**
 * Turns `target = f!(...)` into `target, err = f(...)`, moving from "abort the program on error"
 * to "capture the error and decide". The natural follow-up is to handle `err`, which the
 * existing `.iferr` postfix template writes in full - this intention deliberately stops at the
 * assignment so it stays a single, reviewable edit.
 *
 * The error target is named `err` unless something already in scope is, in which case it becomes
 * `err1`, `err2`, ... - shadowing a live variable here would silently change what a later
 * `if err != null` reads.
 */
class VRLReplaceRaiseWithErrorDestructuringIntention : PsiElementBaseIntentionAction() {

    override fun getFamilyName(): String = "Replace '!' with error destructuring"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val assignment = VRLErrorHandlingForms.enclosingAssignment(element) ?: return false
        val call = VRLErrorHandlingForms.assignedCall(assignment) ?: return false
        if (call.raiseFlag == null) return false
        text = "Replace '!' with error destructuring"
        return true
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val assignment = VRLErrorHandlingForms.enclosingAssignment(element) ?: return
        val call = VRLErrorHandlingForms.assignedCall(assignment) ?: return
        val raiseFlag = call.raiseFlag ?: return
        val target = assignment.orExpr

        val errorName = VRLErrorHandlingForms.freeErrorName(assignment)
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(assignment.containingFile) ?: return

        // Drop the `!` first: editing the later offset before the earlier one keeps the target's
        // own range valid.
        document.deleteString(raiseFlag.textRange.startOffset, raiseFlag.textRange.endOffset)
        document.insertString(target.textRange.endOffset, ", $errorName")
        documentManager.commitDocument(document)
    }
}
