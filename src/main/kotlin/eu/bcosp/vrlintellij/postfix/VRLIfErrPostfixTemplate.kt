package eu.bcosp.vrlintellij.postfix

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement

/**
 * `fallible_call(...).iferr` expands to VRL's standard error-destructuring pattern:
 * ```
 * target, err = fallible_call(...)
 * if err != null {
 * }
 * ```
 * `target` is the existing assignment's LHS if the call was already being assigned to something
 * (`x = fallible_call().iferr`), or `_` for a bare statement.
 */
class VRLIfErrPostfixTemplate(provider: PostfixTemplateProvider) :
    PostfixTemplate(null, "iferr", ".iferr", "target, err = expr\nif err != null {\n}", provider) {

    override fun isApplicable(context: PsiElement, copyDocument: Document, newOffset: Int): Boolean {
        val (_, valueExpr) = postfixTarget(context, newOffset) ?: return false
        return isFallibleCall(valueExpr)
    }

    override fun expand(context: PsiElement, editor: Editor) {
        val project = context.project
        val file = context.containingFile
        val (statement, valueExpr) = postfixTarget(context, editor.caretModel.offset) ?: return
        val target = assignmentTargetText(statement) ?: "_"

        val document = editor.document
        val range = statement.textRange
        val lineStart = document.getLineStartOffset(document.getLineNumber(range.startOffset))
        val baseIndent = document.charsSequence.subSequence(lineStart, range.startOffset).takeWhile { it == ' ' || it == '\t' }
        val innerIndent = "$baseIndent${" ".repeat(CodeStyle.getIndentOptions(file).INDENT_SIZE)}"

        // The blank line's indent is written out by hand: with no statement there yet, there's no
        // PSI node for the formatter to hang an indent off of, and reformatText actively strips a
        // hand-written indent on an otherwise-empty line right back to nothing, so this must be
        // the final text - no reformat pass afterward.
        val prefix = "$target, err = ${valueExpr.text}\n${baseIndent}if err != null {\n"
        val replacement = "$prefix$innerIndent\n$baseIndent}"
        document.replaceString(range.startOffset, range.endOffset, replacement)
        PsiDocumentManager.getInstance(project).commitDocument(document)

        editor.caretModel.moveToOffset(range.startOffset + prefix.length + innerIndent.length)
    }
}
