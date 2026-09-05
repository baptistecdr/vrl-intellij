package eu.bcosp.vrlintellij.formatting

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.formatting.WrapType
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IElementType
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.collapsePassThroughWrappers
import eu.bcosp.vrlintellij.psi.isWhitespace

class VRLBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val indent: Indent,
    private val spacingBuilder: SpacingBuilder,
    private val keepBlankLines: Int,
) : AbstractBlock(node, wrap, alignment) {

    override fun buildChildren(): List<Block> {
        val blocks = mutableListOf<Block>()
        var child = node.firstChildNode
        while (child != null) {
            if (!isWhitespace(child.elementType) && child.textRange.length > 0) {
                blocks.add(
                    VRLBlock(
                        collapsePassThroughWrappers(child),
                        Wrap.createWrap(WrapType.NONE, false),
                        null,
                        childIndent(node.elementType, child.elementType),
                        spacingBuilder,
                        keepBlankLines,
                    )
                )
            }
            child = child.treeNext
        }
        return blocks
    }

    override fun getIndent(): Indent = indent

    // Falls back to a permissive default (force nothing, just cap consecutive blank lines at the
    // user's "Keep Blank Lines" setting) for every pair spacingBuilder has no explicit rule for -
    // e.g. statement-to-statement inside a block, or element-to-element in an array/object -
    // which without this would leave blank lines completely unbounded instead of honoring that
    // setting.
    override fun getSpacing(child1: Block?, child2: Block): Spacing? =
        spacingBuilder.getSpacing(this, child1, child2)
            ?: Spacing.createSpacing(0, Int.MAX_VALUE, 0, true, keepBlankLines)

    override fun isLeaf(): Boolean = node.firstChildNode == null

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        val childIndent = when (node.elementType) {
            VRLElementTypes.BLOCK_EXPR, VRLElementTypes.OBJECT_EXPR, VRLElementTypes.ARRAY_EXPR -> Indent.getNormalIndent()
            else -> Indent.getNoneIndent()
        }
        return ChildAttributes(childIndent, null)
    }

    companion object {
        private val OPEN_CLOSE_DELIMITERS = mapOf(
            VRLElementTypes.BLOCK_EXPR to (VRLElementTypes.LBRACE to VRLElementTypes.RBRACE),
            VRLElementTypes.OBJECT_EXPR to (VRLElementTypes.LBRACE to VRLElementTypes.RBRACE),
            VRLElementTypes.ARRAY_EXPR to (VRLElementTypes.LBRACKET to VRLElementTypes.RBRACKET),
        )

        // Statements/fields/elements nested directly inside a block/object/array literal are
        // indented one level; the delimiters themselves stay at the container's own indent.
        fun childIndent(parentType: IElementType, childType: IElementType): Indent {
            val delimiters = OPEN_CLOSE_DELIMITERS[parentType] ?: return Indent.getNoneIndent()
            return if (childType == delimiters.first || childType == delimiters.second) {
                Indent.getNoneIndent()
            } else {
                Indent.getNormalIndent()
            }
        }
    }
}
