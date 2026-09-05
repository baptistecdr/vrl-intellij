package eu.bcosp.vrlintellij.surround

import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager

/**
 * Shared plumbing for wrapping the selected statement(s) in an `if <condition> { ... }` block
 * (optionally with an `else`): insert the surrounding text directly in the document (simpler and
 * more robust than PSI tree surgery for wrapping an arbitrary already-parsed range), reformat the
 * touched region, and hand back the range of the placeholder condition so the platform selects it
 * for immediate editing - the same UX as every other language's "Surround with if".
 */
abstract class AbstractVRLIfSurrounder : Surrounder {

    protected abstract val suffix: String

    override fun isApplicable(elements: Array<PsiElement>): Boolean = elements.isNotEmpty()

    override fun surroundElements(project: Project, editor: Editor, elements: Array<out PsiElement>): TextRange {
        val document = editor.document
        val start = elements.first().textRange.startOffset
        val end = elements.last().textRange.endOffset
        val prefix = "if $CONDITION_PLACEHOLDER {\n"

        document.insertString(end, suffix)
        document.insertString(start, prefix)
        PsiDocumentManager.getInstance(project).commitDocument(document)

        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
        if (psiFile != null) {
            CodeStyleManager.getInstance(project).reformatText(psiFile, start, end + prefix.length + suffix.length)
        }
        PsiDocumentManager.getInstance(project).commitDocument(document)

        val conditionStart = document.text.indexOf(CONDITION_PLACEHOLDER, start)
        return TextRange(conditionStart, conditionStart + CONDITION_PLACEHOLDER.length)
    }

    companion object {
        private const val CONDITION_PLACEHOLDER = "true"
    }
}

class VRLIfSurrounder : AbstractVRLIfSurrounder() {
    override val suffix = "\n}"
    override fun getTemplateDescription(): String = "if"
}

class VRLIfElseSurrounder : AbstractVRLIfSurrounder() {
    override val suffix = "\n} else {\n\n}"
    override fun getTemplateDescription(): String = "if / else"
}
