package eu.bcosp.vrlintellij.formatting

import com.intellij.lang.ASTNode
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.tree.IElementType
import eu.bcosp.vrlintellij.VRL
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.significantChildren

/**
 * Adds or removes a trailing comma before the closing `]`/`}`/`)` of an array/object literal or a
 * call's argument list, according to the "Use trailing comma" option
 * ([VRLCodeStyleSettings.TRAILING_COMMA], see [VRLLanguageCodeStyleSettingsProvider]) - real,
 * grammar-legal VRL, verified against the `vector vrl` CLI (`x = [1, 2, 3,]` etc. all compile
 * cleanly; see VRL.bnf's `(COMMA x)* COMMA?` for each of the three container shapes).
 *
 * Only ever touches a container that already spans multiple lines - a single-line literal never
 * gets a trailing comma added, matching how this option behaves for other languages (Kotlin,
 * Rust, ...) - and formatting itself never changes a literal's line count, so "multi-line" here
 * just means "the user already broke it across lines".
 */
class VRLTrailingCommaPostFormatProcessor : PostFormatProcessor {

    override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
        if (source.language != VRL) return source
        fixTrailingCommas(source, settings)
        return source
    }

    override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
        if (source.language != VRL) return rangeToReformat
        val lengthBefore = source.textLength
        fixTrailingCommas(source, settings)
        val delta = source.textLength - lengthBefore
        val newEnd = (rangeToReformat.endOffset + delta).coerceAtLeast(rangeToReformat.startOffset)
        return TextRange(rangeToReformat.startOffset, newEnd)
    }

    private class Container(val itemsNode: ASTNode, val openOffset: Int, val closeOffset: Int)

    private fun fixTrailingCommas(root: PsiElement, settings: CodeStyleSettings) {
        val file = root.containingFile ?: return
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return
        val useTrailingComma = settings.getCustomSettings(VRLCodeStyleSettings::class.java).TRAILING_COMMA

        // Collected up front, before any mutation - once fixContainer() starts inserting/removing
        // comma children, only the container currently being fixed is touched, but reading every
        // container's shape first keeps this independent of that (and of tree-walk order) either way.
        val containers = mutableListOf<Container>()
        collectContainers(root.node, containers)

        for (container in containers) {
            fixContainer(container, document, useTrailingComma, root.project)
        }
    }

    private fun collectContainers(node: ASTNode, out: MutableList<Container>) {
        when (node.elementType) {
            VRLElementTypes.ARRAY_EXPR -> addIfDelimited(node, VRLElementTypes.LBRACKET, VRLElementTypes.RBRACKET, node, out)
            VRLElementTypes.OBJECT_EXPR -> addIfDelimited(node, VRLElementTypes.LBRACE, VRLElementTypes.RBRACE, node, out)
            VRLElementTypes.CALL_SUFFIX -> {
                val argumentList = node.findChildByType(VRLElementTypes.ARGUMENT_LIST)
                if (argumentList != null) addIfDelimited(node, VRLElementTypes.LPAREN, VRLElementTypes.RPAREN, argumentList, out)
            }

            else -> Unit
        }
        var child = node.firstChildNode
        while (child != null) {
            collectContainers(child, out)
            child = child.treeNext
        }
    }

    private fun addIfDelimited(delimiterHost: ASTNode, openType: IElementType, closeType: IElementType, itemsNode: ASTNode, out: MutableList<Container>) {
        val open = delimiterHost.findChildByType(openType) ?: return
        val close = delimiterHost.findChildByType(closeType) ?: return
        out += Container(itemsNode, open.startOffset, close.startOffset)
    }

    private fun fixContainer(container: Container, document: Document, useTrailingComma: Boolean, project: Project) {
        // itemsNode is the ARRAY_EXPR/OBJECT_EXPR itself for those two shapes, whose own direct
        // children include the surrounding brackets/braces (unlike ARGUMENT_LIST, which never has
        // its call's parens as children) - excluded here so they're never mistaken for the last
        // real item. COMMA itself is deliberately kept, since detecting an existing trailing one
        // is the whole point of this filter.
        val items = significantChildren(container.itemsNode).filterNot { it.elementType in NON_ITEM_TYPES }
        if (items.isEmpty()) return

        val hasTrailingComma = items.last().elementType == VRLElementTypes.COMMA
        if (hasTrailingComma && items.size < 2) return
        val lastItem = if (hasTrailingComma) items[items.size - 2] else items.last()

        // document offsets are still valid here: fixContainer only ever appends a sibling right
        // after lastItem or deletes the existing trailing comma, and every collected container's
        // own open/close offsets were captured before any of this ran.
        if (container.openOffset >= document.textLength || container.closeOffset > document.textLength) return
        val isMultiLine = document.getLineNumber(container.openOffset) != document.getLineNumber(container.closeOffset)
        val shouldHaveComma = isMultiLine && useTrailingComma

        if (shouldHaveComma && !hasTrailingComma) {
            val comma = createComma(project) ?: return
            lastItem.psi.parent.addAfter(comma, lastItem.psi)
        } else if (!shouldHaveComma && hasTrailingComma) {
            items.last().psi.delete()
        }
    }

    private fun createComma(project: Project): PsiElement? {
        val dummyFile = PsiFileFactory.getInstance(project).createFileFromText("dummy.vrl", VRL, "x = [1,2]")
        return findComma(dummyFile.node)?.psi
    }

    private fun findComma(node: ASTNode): ASTNode? {
        if (node.elementType == VRLElementTypes.COMMA) return node
        var child = node.firstChildNode
        while (child != null) {
            findComma(child)?.let { return it }
            child = child.treeNext
        }
        return null
    }

    companion object {
        private val NON_ITEM_TYPES = setOf(
            VRLElementTypes.LBRACKET,
            VRLElementTypes.RBRACKET,
            VRLElementTypes.LBRACE,
            VRLElementTypes.RBRACE,
            VRLElementTypes.COMMENT,
        )
    }
}
