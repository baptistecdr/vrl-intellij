package eu.bcosp.vrlintellij.moveUpDown

import com.intellij.codeInsight.editorActions.moveUpDown.LineRange
import com.intellij.codeInsight.editorActions.moveUpDown.StatementUpDownMover
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import eu.bcosp.vrlintellij.VRL
import eu.bcosp.vrlintellij.psi.VRLBlockExpr
import eu.bcosp.vrlintellij.psi.VRLFile
import eu.bcosp.vrlintellij.psi.VRLStatement

/**
 * Swaps the statement at the caret with its previous/next sibling as one atomic unit, so moving a
 * multi-line `if` block up or down carries its whole body along instead of tearing it apart -
 * confirmed to be a real problem before this: the platform's generic line-based fallback
 * ([com.intellij.codeInsight.editorActions.moveUpDown.LineMover], `order="before line"` here
 * ensures this mover is tried first) just shifts individual TEXT LINES with no syntax awareness,
 * so moving a multi-line `if` down past a following statement split the `if` header from its own
 * body.
 */
class VRLStatementUpDownMover : StatementUpDownMover() {

    override fun checkAvailable(editor: Editor, file: PsiFile, info: MoveInfo, down: Boolean): Boolean {
        if (file.language != VRL) return false
        val (statements, index) = statementsAtCaret(editor, file) ?: return false
        val targetIndex = if (down) index + 1 else index - 1
        if (targetIndex !in statements.indices) return false

        val current = statements[index]
        val target = statements[targetIndex]
        val document = editor.document

        info.toMove = LineRange(current, current, document)
        info.toMove2 = LineRange(target, target, document)
        return true
    }

    private fun statementsAtCaret(editor: Editor, file: PsiFile): Pair<List<VRLStatement>, Int>? {
        val offset = editor.caretModel.offset.coerceIn(0, (file.textLength - 1).coerceAtLeast(0))
        val elementAtCaret = file.findElementAt(offset) ?: return null
        val statement = PsiTreeUtil.getParentOfType(elementAtCaret, VRLStatement::class.java, false) ?: return null
        val statements = when (val container = statement.parent) {
            is VRLFile -> PsiTreeUtil.getChildrenOfType(container, VRLStatement::class.java)?.toList().orEmpty()
            is VRLBlockExpr -> container.statementList
            else -> return null
        }
        val index = statements.indexOf(statement)
        return if (index < 0) null else statements to index
    }
}
