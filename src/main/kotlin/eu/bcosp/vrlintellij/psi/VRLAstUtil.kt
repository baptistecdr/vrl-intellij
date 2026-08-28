package eu.bcosp.vrlintellij.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

// NEWLINE is a distinct token type from TokenType.WHITE_SPACE (see
// VRLParserDefinition.getWhitespaceTokens), so every "is this just insignificant whitespace"
// check needs to name it explicitly too - these are the shared, single places that do, mirroring
// which of WHITE_SPACE/COMMENT each call site already excluded before NEWLINE existed.
fun isWhitespace(type: IElementType?): Boolean =
    type == TokenType.WHITE_SPACE || type == NEWLINE

fun isWhitespaceOrComment(type: IElementType?): Boolean =
    isWhitespace(type) || type == VRLElementTypes.COMMENT

fun significantChildren(node: ASTNode): List<ASTNode> {
    val result = mutableListOf<ASTNode>()
    var c = node.firstChildNode
    while (c != null) {
        if (!isWhitespace(c.elementType) && c.textRange.length > 0) result.add(c)
        c = c.treeNext
    }
    return result
}

// VRL's expression grammar is a long precedence chain (assignment -> or -> and -> ... -> primary)
// where most rules are pass-through wrappers around a single child whenever their operator isn't
// actually used. Skipping straight to the first node that either has no children (a leaf) or more
// than one significant child (an actual construct) collapses that chain down to the node that's
// actually interesting - originally written to keep the formatter's block tree (and therefore its
// indent math) shallow and correct; also used wherever code needs to identify "what kind of
// expression is this, ignoring precedence wrapping" (e.g. the structure view).
tailrec fun collapsePassThroughWrappers(node: ASTNode): ASTNode {
    val children = significantChildren(node)
    return if (children.size == 1) collapsePassThroughWrappers(children[0]) else node
}
