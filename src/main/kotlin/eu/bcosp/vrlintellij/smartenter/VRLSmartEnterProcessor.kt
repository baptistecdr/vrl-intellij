package eu.bcosp.vrlintellij.smartenter

import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager

/**
 * Completes an `if <condition>` line with no block into `if <condition> {\n    \n}`, caret placed
 * inside. VRL's grammar has no error recovery for a missing block after `if` - `block_expr` is a
 * hard requirement, so an incomplete `if true` doesn't even parse as an if-expression (see
 * [eu.bcosp.vrlintellij.grammars.VRL.bnf]'s `if_expr` rule) - and the platform's own fallback for
 * unhandled Smart Enter just inserts a blank line, leaving the file exactly as broken. This is the
 * one case worth handling explicitly: everywhere else in VRL a block is written inline as part of
 * typing the construct itself, so there's no analogous "forgot the brace" state to complete.
 */
class VRLSmartEnterProcessor : SmartEnterProcessor() {

    override fun process(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val document = editor.document
        val caretOffset = editor.caretModel.offset.coerceIn(0, document.textLength)
        val lineNumber = document.getLineNumber(caretOffset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val trimmedEnd = document.getText(TextRange(lineStart, lineEnd)).trimEnd()

        if (!isIncompleteIfLine(trimmedEnd)) return false

        val insertOffset = lineStart + trimmedEnd.length
        WriteCommandAction.writeCommandAction(project).run<Throwable> {
            document.insertString(insertOffset, " {\n\n}")
        }
        commitDocument(editor)

        val innerLineOffset = insertOffset + " {\n".length
        val caretOffsetAfterIndent = WriteCommandAction.writeCommandAction(project).compute<Int, Throwable> {
            CodeStyleManager.getInstance(project).adjustLineIndent(psiFile, innerLineOffset)
        }
        commitDocument(editor)
        editor.caretModel.moveToOffset(caretOffsetAfterIndent)
        return true
    }

    private fun isIncompleteIfLine(trimmedEnd: String): Boolean {
        val trimmed = trimmedEnd.trimStart()
        if (!trimmed.startsWith("if") || (trimmed.length > 2 && !trimmed[2].isWhitespace())) return false
        if (trimmedEnd.endsWith("{")) return false
        return trimmed.removePrefix("if").isNotBlank()
    }
}
