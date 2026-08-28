package eu.bcosp.vrlintellij.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

// parse_timestamp(value: string, format: string, timezone: string = optional) - two required
// positional/named-capable args plus one optional one, used throughout as a fixed 3-arg function.
class VRLFunctionArgumentsInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(VRLFunctionArgumentsInspection())
    }

    private fun problems(text: String): List<String> {
        myFixture.configureByText("t.vrl", text)
        return myFixture.doHighlighting().mapNotNull { it.description }
    }

    fun testDoesNotFlagAllPositionalCall() {
        assertTrue(problems("x = parse_timestamp(.timestamp, \"%Y\", \"UTC\")\n").isEmpty())
    }

    fun testDoesNotFlagRequiredPositionalWithOptionalOmitted() {
        assertTrue(problems("x = parse_timestamp(.timestamp, \"%Y\")\n").isEmpty())
    }

    fun testDoesNotFlagAllNamedCall() {
        assertTrue(problems("x = parse_timestamp(value: .timestamp, format: \"%Y\")\n").isEmpty())
    }

    fun testDoesNotFlagMixedPositionalAndNamedCall() {
        assertTrue(problems("x = parse_timestamp(.timestamp, format: \"%Y\", timezone: \"UTC\")\n").isEmpty())
    }

    fun testFlagsMissingRequiredArgument() {
        val messages = problems("x = parse_timestamp(.timestamp)\n")
        assertTrue(messages.any { it.contains("Missing required argument") && it.contains("'format'") })
    }

    fun testFlagsTooManyPositionalArguments() {
        val messages = problems("x = parse_timestamp(.timestamp, \"%Y\", \"UTC\", \"extra\")\n")
        assertTrue(messages.any { it.contains("takes at most 3 argument") })
    }

    fun testFlagsUnknownNamedArgument() {
        val messages = problems("x = parse_timestamp(.timestamp, \"%Y\", bogus: \"x\")\n")
        assertTrue(messages.any { it.contains("no argument named 'bogus'") })
    }

    fun testFlagsDuplicateNamedArgument() {
        val messages = problems("x = parse_timestamp(value: .timestamp, value: .other, format: \"%Y\")\n")
        assertTrue(messages.any { it.contains("'value' is specified more than once") })
    }

    fun testFlagsArgumentGivenBothPositionallyAndByName() {
        val messages = problems("x = parse_timestamp(.timestamp, \"%Y\", format: \"%m\")\n")
        assertTrue(messages.any { it.contains("'format' is already given positionally") })
    }

    fun testDoesNotFlagUnknownFunction() {
        assertTrue(problems("x = totally_unknown_function(1, 2, 3)\n").isEmpty())
    }
}
