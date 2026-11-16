package eu.bcosp.vrlintellij

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase
import eu.bcosp.vrlintellij.psi.VRLArrayExpr
import eu.bcosp.vrlintellij.psi.VRLBlockExpr
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLMergeExpr
import eu.bcosp.vrlintellij.psi.VRLNullCoalesceExpr
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
        // Regression check: a naive `(COMMA null_coalesce_expr)?` folded into `assignment_expr` itself would
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

    // ---------------------------------------------------------------------------------------
    // Divergences from real VRL. Every case below was run through `vector vrl` 0.61.0; the
    // grammar it mirrors is vrl's src/parser/parser.lalrpop and src/parser/lex.rs.
    // ---------------------------------------------------------------------------------------

    private fun assertRejected(text: String) {
        assertTrue("expected a parse error for [$text]", assertParseErrors(text).isNotEmpty())
    }

    // `vector vrl` runs `e = .` / `p = 1` / `.q = p` as three statements. A path segment used to
    // be taken across the line break, turning the first two lines into `e = .p = 1`.
    fun testBareDotAtEndOfLineDoesNotSwallowNextLine() {
        val text = "e = .\np = 1\n.q = p\n"
        assertParsesWithoutErrors(text)
        assertEquals(listOf("e = .", "p = 1", ".q = p"), topLevelStatementTexts(text))
    }

    fun testBareMetadataRootAtEndOfLineDoesNotSwallowNextLine() {
        assertEquals(listOf("m = %", "p = 1"), topLevelStatementTexts("m = %\np = 1\n"))
    }

    // lex.rs: `@` is an identifier character anywhere, and an identifier containing it is a
    // PathField token, valid only as a path segment.
    fun testAtSignPathFieldsParse() {
        assertParsesWithoutErrors(".@timestamp = 1\n.x@y = 1\n.x = .@a\n%@x = 1\nx = {\"@a\": 1}\n.y = x.@a\n")
    }

    fun testAtSignIdentifierIsNotAVariable() {
        assertRejected("x@y = 1\n")
    }

    // parser.lalrpop's AnyIdent: keywords and reserved identifiers are valid field names.
    fun testKeywordsAsPathFieldsParse() {
        assertParsesWithoutErrors(".if = 1\n.else = 2\n.null = 3\n.true = 4\n.false = 5\n.abort = 6\n.return = 7\n.break = 8\n")
        assertParsesWithoutErrors("x = {\"if\": 1}\n.y = x.if\n")
    }

    fun testReservedIdentifiersAsPathFieldsParse() {
        assertParsesWithoutErrors(".for = 1\n.while = 2\n.loop = 3\n.continue = 4\n.string = 5\n")
    }

    // `in` is not a VRL keyword at all; `in = 1` compiles.
    fun testInIsAnOrdinaryVariable() {
        assertParsesWithoutErrors("in = 1\n.x = in\n")
    }

    // Reserved identifiers are rejected as variables (`vector vrl`: E205 reserved keyword).
    fun testReservedIdentifiersAreNotVariables() {
        assertRejected("for = 1\n")
        assertRejected("loop = 1\n")
        assertRejected("string = 1\n")
    }

    fun testFunctionNamedLikeAReservedIdentifierStillParses() {
        assertParsesWithoutErrors(".x = string!(.a)\n.y = int(.b) ?? 0\n")
    }

    // parser.lalrpop: Merge sits between Compare and Add.
    fun testObjectMergeParses() {
        val text = ".x = {\"a\": 1} | {\"b\": 2}\n"
        assertParsesWithoutErrors(text)
        val merge = PsiTreeUtil.findChildrenOfType(createPsiFile("t", text), VRLMergeExpr::class.java)
            .single { it.node.findChildByType(VRLElementTypes.PIPE) != null }
        assertEquals("{\"a\": 1} | {\"b\": 2}", merge.text)
    }

    fun testMergeBindsTighterThanComparison() {
        val text = ".x = {\"a\": 1} | {\"b\": 2} == {\"a\": 1, \"b\": 2}\n"
        assertParsesWithoutErrors(text)
        val merge = PsiTreeUtil.findChildrenOfType(createPsiFile("t", text), VRLMergeExpr::class.java)
            .single { it.node.findChildByType(VRLElementTypes.PIPE) != null }
        assertEquals("{\"a\": 1} | {\"b\": 2}", merge.text)
    }

    fun testEmptyClosureParamListParses() {
        assertParsesWithoutErrors(".x = for_each({}) -> || { null }\n")
    }

    fun testBreakParses() {
        assertParsesWithoutErrors("for_each([1]) -> |_i, v| { break }\n")
    }

    // lex.rs: `-` directly followed by a digit is part of a numeric literal; there is no unary
    // minus operator. `.x = -1`, `.a[-1]` and `1 - -2` compile; `3-1`, `- 1`, `-y`, `-(1)` are
    // syntax errors (E203).
    fun testNegativeNumberLiteralsParse() {
        assertParsesWithoutErrors(".x = -1\n.y = -1.5\n.z = .a[-1]\n.w = [1, -2]\n.v = 1 - -2\n.u = 3 - 1\n")
    }

    fun testUnaryMinusIsRejected() {
        assertRejected(".x = 3-1\n")
        assertRejected(".x = - 1\n")
        assertRejected("y = 2\n.x = -y\n")
        assertRejected(".x = -(1)\n")
    }

    fun testDoubleNotParses() {
        assertParsesWithoutErrors(".x = !!true\n.y = ! true\n")
    }

    // lex.rs's string_literal scans to the next unescaped `"`, newlines included.
    fun testStringLiteralSpanningLinesParses() {
        val text = ".x = \"a\nb\"\n.y = 1\n"
        assertParsesWithoutErrors(text)
        assertEquals(listOf(".x = \"a\nb\"", ".y = 1"), topLevelStatementTexts(text))
    }

    // parser.lalrpop: ErrorCoalesce is the loosest binary level, and `||`/`&&` share one level.
    fun testErrorCoalescingBindsLooserThanLogicalOperators() {
        val text = "x = false || null ?? true\n"
        assertParsesWithoutErrors(text)
        val coalesce = PsiTreeUtil.findChildrenOfType(createPsiFile("t", text), VRLNullCoalesceExpr::class.java)
            .single { it.node.findChildByType(VRLElementTypes.NULL_COALESCE) != null }
        assertEquals("false || null", coalesce.logicalExprList.first().text)
    }

    fun testLogicalOperatorsParse() {
        assertParsesWithoutErrors(".x = false && true || true\n.y = .b || .c && false\n")
    }

    fun testNewlineAfterBinaryOperatorContinuesTheExpression() {
        assertEquals(listOf(".x = 1 +\n2"), topLevelStatementTexts(".x = 1 +\n2\n"))
    }

    // `%` only starts a metadata path; VRL has no modulo operator (`mod()` is a function).
    fun testPercentIsNotModulo() {
        assertRejected(".y = 5 % 2\n")
    }

    // parser.lalrpop's Exprs require a newline or `;` between expressions.
    fun testStatementsOnOneLineNeedASeparator() {
        assertRejected(".a = 1 .b = 2\n")
        assertRejected("x = 1 y = 2\n")
        assertParsesWithoutErrors(".a = 1; .b = 2\n")
        assertParsesWithoutErrors(".a = 1;\n.b = 2;\n")
        assertParsesWithoutErrors("if true { .a = 1 }\n")
    }

    fun testMissingSeparatorStillParsesTheNextStatement() {
        assertEquals(listOf(".a = 1", ".b = 2"), topLevelStatementTexts(".a = 1 .b = 2\n"))
    }

    fun testQuestionMarkSuffixIsRejected() {
        assertRejected(".x = .a?\n")
    }

    // parser.lalrpop's ObjectKey is a string literal only.
    fun testBareIdentifierObjectKeyIsRejected() {
        assertRejected(".x = {a: 1}\n")
    }

    // lex.rs's numeric literal has no exponent part.
    fun testFloatExponentIsRejected() {
        assertRejected(".x = 1.5e3\n")
    }

    // `vector vrl`: whitespace never appears inside a path.
    fun testWhitespaceInsideAPathIsRejected() {
        assertRejected("x = {\"b\": 1}\n.y = x .b\n")
        assertRejected("x = {\"b\": 1}\n.y = x. b\n")
        assertRejected(".y = .a .b\n")
        assertRejected(".y = [1] [0]\n")
        assertRejected(".y = parse_json!(\"{}\") .b\n")
    }

    fun testAdjacentPathContinuationsParse() {
        assertParsesWithoutErrors(".y = parse_json!(\"{}\").b\n.z = [1, 2][0]\n.w = {\"b\": 1}.b\n.v = .a.b[0].\"c d\"\n")
    }

    // `vector vrl`: a bare `abort` on its own line doesn't take the next line as its message.
    fun testAbortDoesNotSwallowTheNextLine() {
        assertEquals(listOf("abort", ".x = 1"), topLevelStatementTexts("abort\n.x = 1\n"))
        assertParsesWithoutErrors("abort \"oops\"\n")
    }

    // parser.lalrpop's ReturnExpr requires a value; `return` alone or before a line break is E203.
    fun testReturnNeedsAValueOnTheSameLine() {
        assertParsesWithoutErrors("return .\n")
        assertRejected("return\n")
        assertRejected("if true {\n  return\n  .x\n}\n")
    }

    // lex.rs: digits followed by an identifier character lex as one identifier.
    fun testDigitLeadingNamesParse() {
        assertParsesWithoutErrors(".http.5xx = 1\n")
        assertParsesWithoutErrors("5xx = 1\n.y = 5xx\n")
    }

    // An argument list only follows a function name directly: `vector vrl` rejects all three.
    fun testOnlyAFunctionNameCanBeCalled() {
        assertRejected(".x = 5xx(1)\n")
        assertRejected(".x = .a(1)\n")
        assertRejected(".x = upcase(\"a\")(\"b\")\n")
        assertParsesWithoutErrors(".x = upcase!(\"a\")\n")
    }

    // parser.lalrpop's Predicate::Many: expressions in parentheses separated by `;` or newlines.
    fun testMultiExpressionIfPredicateParses() {
        assertParsesWithoutErrors("if (x = 1; x == 1) { .a = 1 }\n")
        assertParsesWithoutErrors("if (x = 1; x == 1;) { .a = 1 }\n")
        assertParsesWithoutErrors("if (\n  x = 1\n  x == 1\n) { .a = 1 }\n")
        assertParsesWithoutErrors("if (.a == 1) { .b = 2 }\n")
    }

    fun testMissingSeparatorErrorIsShortAndAtTheGap() {
        val errors = assertParseErrors(".a = 1 .b = 2\n")
        assertEquals(listOf("';' or new line expected"), errors.map { it.errorDescription })
        assertEquals(6, errors.single().textOffset)
    }
}
