package eu.bcosp.vrlintellij.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

// encode_base64(value: string, padding?: boolean, charset?: string) whose charset only accepts
// "standard" or "url_safe" - used throughout as a stable, real enum-valued argument.
class VRLInvalidEnumValueInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(VRLInvalidEnumValueInspection())
    }

    private fun problems(text: String): List<String> {
        myFixture.configureByText("t.vrl", text)
        return myFixture.doHighlighting().mapNotNull { it.description }
    }

    fun testDoesNotFlagAValidNamedEnumValue() {
        assertTrue(problems("x = encode_base64(\"hi\", charset: \"url_safe\")\n").isEmpty())
    }

    fun testDoesNotFlagAValidPositionalEnumValue() {
        assertTrue(problems("x = encode_base64(\"hi\", true, \"standard\")\n").isEmpty())
    }

    fun testFlagsAnInvalidNamedEnumValue() {
        val messages = problems("x = encode_base64(\"hi\", charset: \"bogus\")\n")
        assertTrue(messages.any { it.contains("'charset'") && it.contains("'standard'") && it.contains("'url_safe'") && it.contains("got 'bogus'") })
    }

    fun testFlagsAnInvalidPositionalEnumValue() {
        val messages = problems("x = encode_base64(\"hi\", true, \"bogus\")\n")
        assertTrue(messages.any { it.contains("'charset'") && it.contains("got 'bogus'") })
    }

    fun testDoesNotFlagAVariableArgument() {
        assertTrue(problems("cs = \"bogus\"\nx = encode_base64(\"hi\", charset: cs)\n").isEmpty())
    }

    fun testDoesNotFlagAnArgumentWithoutEnumValues() {
        assertTrue(problems("x = encode_base64(\"anything at all\")\n").isEmpty())
    }

    fun testDoesNotFlagUnknownFunction() {
        assertTrue(problems("x = totally_unknown_function(charset: \"bogus\")\n").isEmpty())
    }
}
