package eu.bcosp.vrlintellij.surround

import com.intellij.lang.surroundWith.SurroundDescriptor
import com.intellij.lang.surroundWith.Surrounder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import eu.bcosp.vrlintellij.psi.VRLBlockExpr
import eu.bcosp.vrlintellij.psi.VRLFile
import eu.bcosp.vrlintellij.psi.VRLStatement

/**
 * Feeds "Surround With" (Ctrl+Alt+T / ⌘⌥T) the statement(s) overlapping the current
 * selection (or the single statement at the caret, with no selection) - a script's top-level
 * statements, or a block's, are the only things VRL ever surrounds meaningfully.
 */
class VRLStatementsSurroundDescriptor : SurroundDescriptor {

    override fun getElementsToSurround(file: PsiFile, startOffset: Int, endOffset: Int): Array<PsiElement> {
        val container = containerAt(file, startOffset) ?: return PsiElement.EMPTY_ARRAY
        // A real selection uses half-open overlap (so a statement merely touched at its very
        // boundary by the selection edge isn't included); a bare caret (startOffset == endOffset,
        // e.g. right at a statement's first character) instead needs an inclusive containment
        // check, since a half-open range is empty and would never contain anything.
        val selected = statementsOf(container).filter {
            if (startOffset == endOffset) {
                it.textRange.startOffset <= startOffset && startOffset <= it.textRange.endOffset
            } else {
                it.textRange.startOffset < endOffset && it.textRange.endOffset > startOffset
            }
        }
        return if (selected.isEmpty()) PsiElement.EMPTY_ARRAY else selected.toTypedArray()
    }

    override fun getSurrounders(): Array<Surrounder> = arrayOf(VRLIfSurrounder(), VRLIfElseSurrounder())

    override fun isExclusive(): Boolean = false

    private fun statementsOf(container: PsiElement): List<VRLStatement> = when (container) {
        is VRLFile -> PsiTreeUtil.getChildrenOfType(container, VRLStatement::class.java)?.toList().orEmpty()
        is VRLBlockExpr -> container.statementList
        else -> emptyList()
    }

    private fun containerAt(file: PsiFile, offset: Int): PsiElement? {
        val safeOffset = offset.coerceIn(0, (file.textLength - 1).coerceAtLeast(0))
        var element: PsiElement? = file.findElementAt(safeOffset) ?: return file
        while (element != null && element !is VRLBlockExpr && element !is VRLFile) {
            element = element.parent
        }
        return element
    }
}
