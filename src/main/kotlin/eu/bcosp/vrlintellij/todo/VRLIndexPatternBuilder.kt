package eu.bcosp.vrlintellij.todo

import com.intellij.lexer.Lexer
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.search.IndexPatternBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import eu.bcosp.vrlintellij.VRLFileType
import eu.bcosp.vrlintellij.grammars.VRLLexerAdapter
import eu.bcosp.vrlintellij.highlighting.VRLHighlightingTokenSets

/**
 * Lets `# TODO`-style comments in `.vrl` files show up in the TODO tool window and be found by
 * "Search for TODOs" - without this, VRL's comments (recognized by the parser via
 * [eu.bcosp.vrlintellij.psi.VRLParserDefinition.getCommentTokens]) are invisible to the TODO
 * indexer, which scans each file type's comment tokens independently.
 */
class VRLIndexPatternBuilder : IndexPatternBuilder {

    override fun getIndexingLexer(file: PsiFile): Lexer? =
        if (file.fileType == VRLFileType) VRLLexerAdapter() else null

    override fun getCommentTokenSet(file: PsiFile): TokenSet? =
        if (file.fileType == VRLFileType) VRLHighlightingTokenSets.COMMENT else null

    override fun getCommentStartDelta(tokenType: IElementType?): Int = 0

    override fun getCommentEndDelta(tokenType: IElementType?): Int = 0
}
