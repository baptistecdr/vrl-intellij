package eu.bcosp.vrlintellij

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase
import eu.bcosp.vrlintellij.psi.VRLArrayExpr
import eu.bcosp.vrlintellij.psi.VRLParserDefinition

class VRLParsingTest : ParsingTestCase("", "vrl", VRLParserDefinition()) {
    override fun getTestDataPath(): String = "src/test/testData"

    override fun includeRanges(): Boolean = true

    private fun assertParsesWithoutErrors(text: String) {
        val file = createPsiFile("t", text)
        val errors = mutableListOf<PsiErrorElement>()
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is PsiErrorElement) errors.add(element)
                super.visitElement(element)
            }
        })
        assertTrue(
            "expected no parse errors in [$text], got: ${errors.map { it.errorDescription }}",
            errors.isEmpty()
        )
    }

    fun testClosureAttachedToCallWithArrowParses() {
        // Real VRL iteration syntax: `for_each`/`map_values`/etc. take a closure via `->`.
        // ARROW was declared in the lexer but unused by the grammar until this was fixed.
        assertParsesWithoutErrors("map_values(.) -> |key, value| { upcase(value) }\n")
    }

    fun testFallibleCallWithRaiseFlagParses() {
        assertParsesWithoutErrors("x = parse_json!(.message)\n")
    }

    fun testNamedArgumentsParse() {
        assertParsesWithoutErrors("split(\"a,b\", pattern: \",\")\n")
    }

    fun testForInLoopParses() {
        assertParsesWithoutErrors("for x in y { }\n")
    }

    fun testMetadataPathParses() {
        assertParsesWithoutErrors("x = %datadog_api_key\n")
    }

    fun testQuotedPathSegmentParses() {
        assertParsesWithoutErrors(".\"my-weird-key\" = 1\n")
    }

    fun testQuotedPostfixSegmentParses() {
        assertParsesWithoutErrors("x = .foo.\"bar baz\".qux\n")
    }

    fun testMultiTargetErrorDestructuringAssignmentParses() {
        assertParsesWithoutErrors("value, err = parse_json(.message)\n")
    }

    fun testArrayLiteralElementCountIsNotSwallowedByMultiTargetAssignment() {
        // Regression check: a naive `(COMMA or_expr)?` folded into `assignment_expr` itself would
        // greedily consume the comma in `[a, b]` too, silently collapsing it into a 1-element
        // array whose element is a bogus "a, b" node instead of a proper 2-element array.
        val file = createPsiFile("t", "x = [a, b]\n")
        val arrayExpr = PsiTreeUtil.findChildOfType(file, VRLArrayExpr::class.java)
        assertNotNull(arrayExpr)
        assertEquals(2, arrayExpr!!.expressionList.size)
    }

    fun testArgumentListIsNotSwallowedByMultiTargetAssignment() {
        assertParsesWithoutErrors("split(\"a,b\", pattern: \",\")\n")
    }
}