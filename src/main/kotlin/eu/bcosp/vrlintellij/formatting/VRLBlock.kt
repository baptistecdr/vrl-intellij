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
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IElementType
import eu.bcosp.vrlintellij.psi.VRLElementTypes

class VRLBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val indent: Indent,
    private val spacingBuilder: SpacingBuilder,
) : AbstractBlock(node, wrap, alignment) {

    override fun buildChildren(): List<Block> {
        val blocks = mutableListOf<Block>()
        var child = node.firstChildNode
        while (child != null) {
            if (child.elementType != TokenType.WHITE_SPACE && child.textRange.length > 0) {
                blocks.add(
                    VRLBlock(
                        collapsePassThroughWrappers(child),
                        Wrap.createWrap(WrapType.NONE, false),
                        null,
                        childIndent(node.elementType, child.elementType),
                        spacingBuilder,
                    )
                )
            }
            child = child.treeNext
        }
        return blocks
    }

    override fun getIndent(): Indent = indent

    override fun getSpacing(child1: Block?, child2: Block): Spacing? = spacingBuilder.getSpacing(this, child1, child2)

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

        private fun significantChildren(node: ASTNode): List<ASTNode> {
            val result = mutableListOf<ASTNode>()
            var c = node.firstChildNode
            while (c != null) {
                if (c.elementType != TokenType.WHITE_SPACE && c.textRange.length > 0) result.add(c)
                c = c.treeNext
            }
            return result
        }

        // VRL's expression grammar is a long precedence chain (assignment -> or -> and ->
        // ... -> primary) where most rules are pass-through wrappers around a single child
        // whenever their operator isn't actually used. Left uncollapsed, a single literal ends
        // up wrapped in a dozen same-position Block levels, which confuses the platform's indent
        // accumulation (each "None"-indent level along that chain still counts as an anchor,
        // compounding the container's indent by the number of wrapper levels rather than by the
        // number of Normal-indent hops). Skipping straight to the first node that either has no
        // children (a leaf) or more than one significant child (an actual construct) keeps the
        // block tree - and therefore the indent math - shallow and correct. This never affects
        // spacing rules keyed on parent composite types (aroundInside(...)), since those only
        // match parents that truly have multiple children (an operator being present) and are
        // therefore never collapsed away.
        private tailrec fun collapsePassThroughWrappers(node: ASTNode): ASTNode {
            val children = significantChildren(node)
            return if (children.size == 1) collapsePassThroughWrappers(children[0]) else node
        }
    }
}
