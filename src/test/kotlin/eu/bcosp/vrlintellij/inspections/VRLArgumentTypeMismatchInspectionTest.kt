package eu.bcosp.vrlintellij.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

// parse_timestamp(value: string, format: string, timezone?: string) and
// split(value: string, pattern: string|regex, limit?: integer) - used throughout since both have
// clearly single- or union-typed parameters.
class VRLArgumentTypeMismatchInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(VRLArgumentTypeMismatchInspection())
    }

    private fun problems(text: String): List<String> {
        myFixture.configureByText("t.vrl", text)
        return myFixture.doHighlighting().mapNotNull { it.description }
    }

    fun testDoesNotFlagMatchingStringArgument() {
        assertTrue(problems("x = parse_timestamp(\"2021-01-01\", \"%Y-%m-%d\")\n").isEmpty())
    }

    fun testFlagsIntegerWherePositionalStringExpected() {
        val messages = problems("x = parse_timestamp(123, \"%Y-%m-%d\")\n")
        assertTrue(messages.any { it.contains("'value'") && it.contains("expects string") && it.contains("got integer") })
    }

    fun testDoesNotFlagRegexForAUnionTypedParameter() {
        assertTrue(problems("x = split(\"a,b\", r'[,]')\n").isEmpty())
    }

    fun testFlagsArrayForAUnionTypedParameter() {
        val messages = problems("x = split([1, 2], \",\")\n")
        assertTrue(messages.any { it.contains("'value'") && it.contains("got array") })
    }

    fun testDoesNotFlagAVariableArgument() {
        assertTrue(problems("x = 1\ny = parse_timestamp(x, \"%Y\")\n").isEmpty())
    }

    fun testDoesNotFlagAFunctionCallArgument() {
        assertTrue(problems("x = parse_timestamp(upcase(\"x\"), \"%Y\")\n").isEmpty())
    }

    fun testFlagsNamedArgumentTypeMismatch() {
        val messages = problems("x = parse_timestamp(value: 123, format: \"%Y\")\n")
        assertTrue(messages.any { it.contains("'value'") && it.contains("got integer") })
    }

    fun testDoesNotFlagUnknownFunction() {
        assertTrue(problems("x = totally_unknown_function(1, 2, 3)\n").isEmpty())
    }

    fun testFlagsOptionalArgumentTypeMismatch() {
        val messages = problems("x = parse_timestamp(\"2021-01-01\", \"%Y-%m-%d\", 123)\n")
        assertTrue(messages.any { it.contains("'timezone'") && it.contains("got integer") })
    }

    fun testFlagsNullLiteralWhereNotAccepted() {
        val messages = problems("x = parse_timestamp(null, \"%Y\")\n")
        assertTrue(messages.any { it.contains("'value'") && it.contains("got null") })
    }
}
