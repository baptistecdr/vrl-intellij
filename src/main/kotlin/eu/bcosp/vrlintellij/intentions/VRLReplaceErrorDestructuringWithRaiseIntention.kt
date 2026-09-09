package eu.bcosp.vrlintellij.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import eu.bcosp.vrlintellij.psi.VRLMultiAssignmentExpr
import eu.bcosp.vrlintellij.references.VRLVariableResolver

/**
 * Turns `target, err = f(...)` into `target = f!(...)`, but only when nothing ever reads `err`.
 *
 * This changes behaviour on purpose: capturing an error nobody looks at means failures pass
 * silently and `target` is left null, while raising aborts. When `err` is genuinely unread the
 * silent version is almost always the accident, so the intention offers the loud one - and stays
 * hidden the moment `err` is read, where removing it would break the code rather than fix it.
 *
 * "Is this ever read" comes from [VRLVariableResolver.readDeclarations], the same model the unused
 * variable inspection uses, so the two can't disagree about the same variable.
 */
class VRLReplaceErrorDestructuringWithRaiseIntention : PsiElementBaseIntentionAction() {

    override fun getFamilyName(): String = "Replace error destructuring with '!'"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val assignment = VRLErrorHandlingForms.enclosingMultiAssignment(element) ?: return false
        if (!isConvertible(assignment)) return false
        text = "Replace error destructuring with '!'"
        return true
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val assignment = VRLErrorHandlingForms.enclosingMultiAssignment(element) ?: return
        if (!isConvertible(assignment)) return
        val call = VRLErrorHandlingForms.assignedCall(assignment) ?: return
        val targets = assignment.orExprList
        val value = targets.getOrNull(0) ?: return
        val error = targets.getOrNull(1) ?: return

        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(assignment.containingFile) ?: return

        // Latest offset first so the earlier ranges stay valid: add the `!` at the call, then
        // remove `, err` between the two targets.
        document.insertString(call.textRange.startOffset, "!")
        document.deleteString(value.textRange.endOffset, error.textRange.endOffset)
        documentManager.commitDocument(document)
    }

    private fun isConvertible(assignment: VRLMultiAssignmentExpr): Boolean {
        val call = VRLErrorHandlingForms.assignedCall(assignment) ?: return false
        // Already raising - there is nothing to convert, and `f!!()` isn't a thing.
        if (call.raiseFlag != null) return false

        val targets = assignment.orExprList
        if (targets.size != 2) return false

        // `_ = f!()` is compiler error 640 ("unnecessary no-op assignment"), so a discarded value
        // target has no single-assignment form to convert into.
        val value = VRLErrorHandlingForms.targetPrimaryExpr(targets[0])
        if (value?.identifier?.text == "_") return false

        val error = VRLErrorHandlingForms.targetPrimaryExpr(targets[1]) ?: return false
        val errorIdentifier = error.identifier ?: return false
        if (errorIdentifier.text == "_") return true

        return error !in VRLVariableResolver.readDeclarations(assignment.containingFile)
    }
}
