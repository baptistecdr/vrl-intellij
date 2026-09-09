package eu.bcosp.vrlintellij.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLLiteral

/**
 * Switches the string literal at the caret between VRL's interpreted (`"..."`) and raw (`s'...'`)
 * forms, re-escaping the contents so the value stays identical.
 *
 * Converting to a raw string isn't always possible, and the intention simply doesn't appear in
 * those cases rather than quietly changing the program - see [VRLStringLiteralConversion] for the
 * three blockers (an interpolation, a `'`, or a control character) and the `vector vrl` evidence
 * behind each.
 */
class VRLConvertStringLiteralIntention : PsiElementBaseIntentionAction() {

    override fun getFamilyName(): String = "Convert string literal"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val literal = stringLiteral(element) ?: return false
        return when (literal.node.firstChildNode?.elementType) {
            VRLElementTypes.STRING -> {
                if (VRLStringLiteralConversion.interpretedToRaw(contents(literal)) == null) return false
                text = "Convert to raw string s'...'"
                true
            }

            VRLElementTypes.RAW_STRING -> {
                text = "Convert to interpreted string \"...\""
                true
            }

            else -> false
        }
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val literal = stringLiteral(element) ?: return
        val contents = contents(literal)
        val replacement = when (literal.node.firstChildNode?.elementType) {
            VRLElementTypes.STRING -> VRLStringLiteralConversion.interpretedToRaw(contents)?.let { "s'$it'" }
            VRLElementTypes.RAW_STRING -> "\"${VRLStringLiteralConversion.rawToInterpreted(contents)}\""
            else -> null
        } ?: return

        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(literal.containingFile) ?: return
        val range = literal.textRange
        document.replaceString(range.startOffset, range.endOffset, replacement)
        documentManager.commitDocument(document)
    }

    private fun stringLiteral(element: PsiElement): VRLLiteral? {
        val literal = PsiTreeUtil.getParentOfType(element, VRLLiteral::class.java, false) ?: return null
        val kind = literal.node.firstChildNode?.elementType
        return literal.takeIf { kind == VRLElementTypes.STRING || kind == VRLElementTypes.RAW_STRING }
    }

    /**
     * The literal's text without its delimiters - one character of prefix for `"`, two for `s'`,
     * and a closing quote that may be missing while the literal is still being typed.
     */
    private fun contents(literal: VRLLiteral): String {
        val text = literal.text
        val prefix = if (literal.node.firstChildNode?.elementType == VRLElementTypes.RAW_STRING) 2 else 1
        val hasClosingQuote = text.length > prefix && (text.last() == '"' || text.last() == '\'')
        val end = (if (hasClosingQuote) text.length - 1 else text.length).coerceAtLeast(prefix)
        return text.substring(prefix.coerceAtMost(end), end)
    }
}
