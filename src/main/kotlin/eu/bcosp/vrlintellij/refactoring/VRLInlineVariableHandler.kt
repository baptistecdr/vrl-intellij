package eu.bcosp.vrlintellij.refactoring

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.util.CommonRefactoringUtil
import eu.bcosp.vrlintellij.VRL
import eu.bcosp.vrlintellij.psi.VRLAssignmentExpr
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLPrimaryExpr
import eu.bcosp.vrlintellij.psi.VRLStatement
import eu.bcosp.vrlintellij.psi.collapsePassThroughWrappers
import eu.bcosp.vrlintellij.psi.significantChildren
import eu.bcosp.vrlintellij.references.VRLVariableResolver

/**
 * The inverse of [VRLIntroduceVariableHandler]: every usage of a `name = <expr>` variable is
 * replaced with `<expr>`, and the declaration statement is removed. Deliberately scoped to a
 * single-target bare assignment that is itself a whole statement - not a
 * `value, err = fallible_call()` target (there's no single expression to substitute one of two
 * targets with) and not a chained assignment (`x = y = 1` - substituting its RHS `y = 1` verbatim
 * at every usage would silently re-run that assignment's side effect at each one).
 *
 * Every usage is substituted regardless of how many there are or whether the RHS could have side
 * effects (a fallible call, a block with field writes) - the same "just do it" behavior most IDEs'
 * Inline Variable already has for a plain local. This mirrors [VRLVariableResolver]'s own
 * documented approximation elsewhere in this plugin: correct for the common case, not soundness in
 * the general one.
 */
class VRLInlineVariableHandler : InlineActionHandler() {

    override fun isEnabledForLanguage(language: Language): Boolean = language == VRL

    override fun canInlineElement(element: PsiElement): Boolean {
        val target = element as? VRLPrimaryExpr ?: return false
        return singleAssignmentFor(target) != null
    }

    override fun inlineElement(project: Project, editor: Editor, element: PsiElement) {
        val target = element as? VRLPrimaryExpr ?: return
        val assignment = singleAssignmentFor(target)
        val statement = assignment?.let { PsiTreeUtil.getParentOfType(it, VRLStatement::class.java) }
        val valueExpr = assignment?.assignmentExpr
        if (assignment == null || statement == null || valueExpr == null) {
            showError(project, editor, "Cannot inline this variable")
            return
        }

        val usages = ReferencesSearch.search(target).findAll()
        if (usages.isEmpty()) {
            showError(project, editor, "Variable '${target.text}' has no usages to inline")
            return
        }

        // A parenthesized substitution is always valid wherever the usage sat (an operand of a
        // binary/unary operator, or the receiver of a postfix `.field`/`[...]`/call suffix) - so
        // parens are added unless the RHS is already safe to drop in as-is, rather than analyzing
        // each usage's own context.
        val replacementText = if (isPostfixExprOrTighter(valueExpr.node)) valueExpr.text else "(${valueExpr.text})"

        val document = editor.document
        val statementRange = statement.textRange
        val lineStart = document.getLineStartOffset(document.getLineNumber(statementRange.startOffset))
        val linePrefix = document.charsSequence.subSequence(lineStart, statementRange.startOffset)
        val deleteStart = if (linePrefix.isBlank()) lineStart else statementRange.startOffset
        var deleteEnd = statementRange.endOffset
        if (deleteEnd < document.textLength && document.charsSequence[deleteEnd] == '\n') deleteEnd++

        val edits = usages.map { Triple(it.element.textRange.startOffset, it.element.textRange.endOffset, replacementText) } +
            Triple(deleteStart, deleteEnd, "")

        WriteCommandAction.runWriteCommandAction(project, RefactoringBundle.message("inline.variable.title"), null, {
            for ((start, end, replacement) in edits.sortedByDescending { it.first }) {
                document.replaceString(start, end, replacement)
            }
            PsiDocumentManager.getInstance(project).commitDocument(document)
        })
    }

    /** The enclosing `name = <expr>` when [target] is its bare, whole-statement, non-chained
     * assignment target - null for anything else (a multi-target assignment, an assignment nested
     * inside a larger expression, or one whose own RHS is itself a further assignment). */
    private fun singleAssignmentFor(target: VRLPrimaryExpr): VRLAssignmentExpr? {
        if (!VRLVariableResolver.isBareAssignmentTarget(target)) return null
        val assignment = PsiTreeUtil.getParentOfType(target, VRLAssignmentExpr::class.java) ?: return null
        // Collapsing a bare identifier's precedence-wrapper chain descends past the primary_expr
        // composite itself, all the way to its sole child - the raw identifier leaf token (a
        // primary_expr's own single IDENTIFIER child is, in turn, just one more pass-through
        // level) - so the comparison has to be leaf-to-leaf, not leaf-to-composite.
        if (collapsePassThroughWrappers(assignment.orExpr.node) != target.identifier?.node) return null
        if (assignment.assignmentExpr?.assignmentExpr != null) return null

        val statement = PsiTreeUtil.getParentOfType(assignment, VRLStatement::class.java) ?: return null
        if (statement.expression?.assignmentExpr != assignment) return null
        return assignment
    }

    // True once the pass-through chain reaches a POSTFIX_EXPR - at or below that level (a bare
    // literal/path/variable, a function call, an already-parenthesized expression, and any of
    // those with postfix suffixes chained on) a substituted usage can always take a *further*
    // postfix suffix directly, the same way `x.a.b` already chains suffixes without parens.
    // Anything above that level (a real unary/binary/assignment operator) needs parens: not just
    // for precedence, but because a postfix suffix (`.field`, `[...]`, a call) only ever attaches
    // to a primary_expr - stopping short of one, `-x.a` would parse as `-(x.a)` rather than
    // `(-x).a`, and `1 + 2.a` wouldn't parse as intended at all.
    private tailrec fun isPostfixExprOrTighter(node: ASTNode): Boolean {
        if (node.elementType == VRLElementTypes.POSTFIX_EXPR) return true
        val children = significantChildren(node)
        if (children.size != 1) return false
        return isPostfixExprOrTighter(children[0])
    }

    private fun showError(project: Project, editor: Editor, message: String) {
        CommonRefactoringUtil.showErrorHint(project, editor, message, RefactoringBundle.message("inline.variable.title"), null)
    }
}
