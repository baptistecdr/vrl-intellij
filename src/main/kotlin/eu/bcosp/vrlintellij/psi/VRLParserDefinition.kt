package eu.bcosp.vrlintellij.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import eu.bcosp.vrlintellij.VRL
import eu.bcosp.vrlintellij.grammars.VRLLexerAdapter
import eu.bcosp.vrlintellij.highlighting.VRLHighlightingTokenSets
import eu.bcosp.vrlintellij.parser.VRLParser

private val FILE = IFileElementType(VRL)

class VRLParserDefinition: ParserDefinition {
    override fun createLexer(project: Project): Lexer {
        return VRLLexerAdapter()
    }

    override fun createParser(project: Project): PsiParser {
        return VRLParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun getCommentTokens(): TokenSet {
        return VRLHighlightingTokenSets.COMMENT
    }

    override fun getStringLiteralElements(): TokenSet {
        return TokenSet.EMPTY
    }

    override fun createElement(node: ASTNode): PsiElement {
        return VRLElementTypes.Factory.createElement(node)
    }

    override fun createFile(provider: FileViewProvider): PsiFile {
        return VRLFile(provider)
    }
}
