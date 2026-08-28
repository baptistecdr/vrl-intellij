package eu.bcosp.vrlintellij.refactoring

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.util.CommonRefactoringUtil
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLStatement

/**
 * A deliberately simple "Introduce Variable": the user selects an exact expression, a new
 * `name = <selection>` statement is inserted immediately before the enclosing statement, and the
 * selection is replaced with `name`. The new name is left selected in the editor so the user can
 * type over it immediately - a lighter substitute for the platform's full in-place-rename template
 * machinery, in the same spirit as this plugin's other editor-driven transforms (postfix templates,
 * quick fixes).
 */
class VRLIntroduceVariableHandler : RefactoringActionHandler {

    override fun invoke(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext?) {
        val selectionModel = editor.selectionModel
        if (!selectionModel.hasSelection()) {
            showError(project, editor, "Select an expression to extract into a variable")
            return
        }

        val start = selectionModel.selectionStart
        val end = selectionModel.selectionEnd
        val target = exactSelectionElement(file, start, end)
        if (target == null) {
            showError(project, editor, "Selection must exactly cover a single expression")
            return
        }

        // `strict = false`: a statement with no distinguishing surrounding token (no trailing
        // `;`) has the exact same text range as its own expression, so `target` may already
        // legitimately be the VRLStatement itself - comparing ranges below (not identity) is
        // what actually catches "the whole statement was selected", regardless of which PSI
        // subtype the range-walk in `exactSelectionElement` happened to land on.
        val statement = PsiTreeUtil.getParentOfType(target, VRLStatement::class.java, false)
        if (statement == null || target.textRange == statement.textRange) {
            showError(project, editor, "Cannot introduce a variable for a whole statement")
            return
        }

        val document = editor.document
        val exprText = target.text
        val name = suggestName(file)
        val statementStart = statement.textRange.startOffset
        val lineStart = document.getLineStartOffset(document.getLineNumber(statementStart))
        val baseIndent = document.charsSequence.subSequence(lineStart, statementStart).takeWhile { it == ' ' || it == '\t' }

        WriteCommandAction.runWriteCommandAction(project, RefactoringBundle.message("introduce.variable.title"), null, {
            // Applied highest offset first so `statementStart` (below `start`) stays valid.
            document.replaceString(start, end, name)
            document.insertString(statementStart, "$name = $exprText\n$baseIndent")
            PsiDocumentManager.getInstance(project).commitDocument(document)

            editor.selectionModel.setSelection(statementStart, statementStart + name.length)
            editor.caretModel.moveToOffset(statementStart + name.length)
        })
    }

    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
        // Introduce Variable is only meaningful from an editor selection.
    }

    private fun showError(project: Project, editor: Editor, message: String) {
        CommonRefactoringUtil.showErrorHint(project, editor, message, RefactoringBundle.message("introduce.variable.title"), null)
    }

    private fun exactSelectionElement(file: PsiFile, start: Int, end: Int): PsiElement? {
        var element = file.findElementAt(start) ?: return null
        while (element.textRange.endOffset < end) {
            element = element.parent ?: return null
        }
        return element.takeIf { it.textRange.startOffset == start && it.textRange.endOffset == end }
    }

    private fun suggestName(file: PsiFile): String {
        val used = mutableSetOf<String>()
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType == VRLElementTypes.IDENTIFIER) used.add(element.text)
                super.visitElement(element)
            }
        })
        var candidate = "value"
        var suffix = 2
        while (candidate in used) {
            candidate = "value$suffix"
            suffix++
        }
        return candidate
    }
}
