package eu.bcosp.vrlintellij

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase
import eu.bcosp.vrlintellij.psi.VRLArrayExpr
import eu.bcosp.vrlintellij.psi.VRLBlockExpr
import eu.bcosp.vrlintellij.psi.VRLParserDefinition
import eu.bcosp.vrlintellij.psi.VRLStatement

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

    private fun assertParseErrors(text: String): List<PsiErrorElement> {
        val file = createPsiFile("t", text)
        val errors = mutableListOf<PsiErrorElement>()
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is PsiErrorElement) errors.add(element)
                super.visitElement(element)
            }
        })
        return errors
    }

    fun testBracketIntegerIndexParses() {
        assertParsesWithoutErrors(".x = .items[0]\n")
    }

    // Regression coverage for a real bug: VRL's bracket indexing is integer-literal only - there's
    // no dynamic/computed indexing by a string or variable (confirmed against the vrl CLI, which
    // rejects both with "expected: integer literal"). The grammar used to accept a STRING inside
    // brackets as an alternative to a dotted string segment (`.foo["bar"]` alongside the correct
    // `.foo."bar"`), silently letting the IDE accept code the real compiler rejects outright.
    fun testBracketStringIndexIsRejected() {
        assertTrue(
            "expected a parse error for `.x = .request[\"remote_addr\"]`",
            assertParseErrors(".x = .request[\"remote_addr\"]\n").isNotEmpty(),
        )
    }

    fun testBracketVariableIndexIsRejected() {
        assertTrue(
            "expected a parse error for `.x = .items[i]`",
            assertParseErrors("i = 0\n.x = .items[i]\n").isNotEmpty(),
        )
    }

    fun testForWhileLoopAreNotRealVrlSyntaxAndFailToParse() {
        // `for`/`while`/`loop`/`break`/`continue` are reserved keywords in real VRL (it's
        // deliberately not Turing complete - see vector.dev's VRL reference) but have no actual
        // expression grammar; iteration only happens via functions + closures (`for_each(...) ->
        // |k, v| { }`). Confirm these keywords are rejected rather than silently accepted as loops.
        val file = createPsiFile("t", "for x in y { }\n")
        val errors = mutableListOf<PsiErrorElement>()
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is PsiErrorElement) errors.add(element)
                super.visitElement(element)
            }
        })
        assertTrue("expected a parse error for `for x in y { }`", errors.isNotEmpty())
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

    fun testTrailingCommaInObjectLiteralParses() {
        assertParsesWithoutErrors(". = {\n    \"message\": \"Hello World\",\n    \"old_dot\": .,\n}\n")
    }

    fun testTrailingCommaInArrayLiteralParses() {
        assertParsesWithoutErrors("x = [1, 2, 3,]\n")
    }

    fun testTrailingCommaInArgumentListParses() {
        assertParsesWithoutErrors("split(\"a,b\", pattern: \",\",)\n")
    }

    private fun topLevelStatementTexts(text: String): List<String> {
        val file = createPsiFile("t", text)
        return PsiTreeUtil.getChildrenOfType(file, VRLStatement::class.java)?.map { it.text } ?: emptyList()
    }

    // Regression coverage for a real, previously-silent bug: a bare newline (no `;`) is supposed
    // to separate statements per the VRL reference ("expressions can be separated by newline or
    // semicolon in any combination"), but a value followed by a newline then `.`/`[`/`?`/`(` used
    // to be swallowed as a postfix continuation of that value instead - e.g. `x = 1\n.foo = 2`
    // parsed as one statement `x = (1.foo = 2)`. See VRL.bnf's `<<newlineBefore>>` guards.
    fun testNewlineSeparatesTwoAssignmentsWhereFirstValueCouldTakeADotContinuation() {
        assertEquals(listOf("x = 1", ".foo = 2"), topLevelStatementTexts("x = 1\n.foo = 2\n"))
    }

    fun testNewlineSeparatesStatementsAcrossABracketPostfixContinuation() {
        assertEquals(listOf("x = y", "[0] = 1"), topLevelStatementTexts("x = y\n[0] = 1\n"))
    }

    fun testNewlineBlocksAQuestionContinuation() {
        // A bare `?` can't start a fresh statement on its own (it's only a postfix suffix), so
        // this input is expected to still report a parse error for the dangling `?` - what this
        // actually checks is that `y` didn't get silently extended into `y?` in the first place.
        assertEquals("x = y", topLevelStatementTexts("x = y\n?\n").first())
    }

    fun testNewlineSeparatesStatementsAcrossACallContinuation() {
        assertEquals(listOf("x = y", "upcase(\"a\")"), topLevelStatementTexts("x = y\nupcase(\"a\")\n"))
    }

    fun testNewlineBlocksABinaryOperatorContinuation() {
        // `*` can't start a fresh statement either, so `* 2` is expected to dangle as a parse
        // error - this checks that `1` wasn't first silently extended into `1 * 2`.
        assertEquals("x = 1", topLevelStatementTexts("x = 1\n* 2\n").first())
    }

    fun testSameLinePostfixContinuationStillWorks() {
        assertParsesWithoutErrors("x = .foo.bar\n")
        assertEquals(listOf("x = .foo.bar"), topLevelStatementTexts("x = .foo.bar\n"))
    }

    fun testSameLineBinaryOperatorContinuationStillWorks() {
        assertEquals(listOf("x = 1 + 2"), topLevelStatementTexts("x = 1 + 2\n"))
    }

    fun testNewlineWithinBlockStatementsAreSeparateStatements() {
        // The exact promo.vrl style: several statements inside a block, one per line, no `;`.
        val text = "if err != null {\n.parse_error = true\n.raw = .message\nabort\n}\n"
        assertParsesWithoutErrors(text)
        val file = createPsiFile("t", text)
        val block = PsiTreeUtil.findChildOfType(file, VRLBlockExpr::class.java)!!
        assertEquals(listOf(".parse_error = true", ".raw = .message", "abort"), block.statementList.map { it.text })
    }

    fun testMultiLineArrayLiteralWithoutTrailingCommaStillParses() {
        assertParsesWithoutErrors("x = [\n1,\n2\n]\n")
    }

    fun testMultiLineObjectLiteralWithoutTrailingCommaStillParses() {
        assertParsesWithoutErrors("x = {\n\"a\": 1,\n\"b\": 2\n}\n")
    }

    fun testMultiLineArgumentListWithoutTrailingCommaStillParses() {
        assertParsesWithoutErrors("split(\n\"a,b\",\npattern: \",\"\n)\n")
    }

    fun testElseOnItsOwnLineStillParses() {
        assertParsesWithoutErrors("if true {\nx = 1\n}\nelse {\nx = 2\n}\n")
    }
}