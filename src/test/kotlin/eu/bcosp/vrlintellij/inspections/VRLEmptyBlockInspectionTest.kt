package eu.bcosp.vrlintellij.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLEmptyBlockInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(VRLEmptyBlockInspection())
    }

    private fun problems(text: String): List<String> {
        myFixture.configureByText("t.vrl", text)
        return myFixture.doHighlighting().mapNotNull { it.description }
    }

    fun testFlagsEmptyIfBody() {
        val messages = problems("if true {\n}\n")
        assertTrue(messages.any { it.contains("Block cannot be empty") })
    }

    fun testFlagsEmptyElseBody() {
        val messages = problems("if true {\nx = 1\n} else {\n}\n")
        assertEquals(1, messages.count { it.contains("Block cannot be empty") })
    }

    fun testFlagsEmptyClosureBody() {
        val messages = problems("map_values(.) -> |v| {\n}\n")
        assertTrue(messages.any { it.contains("Block cannot be empty") })
    }

    fun testFlagsEmptyBlockUsedAsExpressionValue() {
        val messages = problems("x = {\n}\n")
        assertTrue(messages.any { it.contains("Block cannot be empty") })
    }

    fun testDoesNotFlagNonEmptyIfBody() {
        assertTrue(problems("if true {\nx = 1\n}\n").isEmpty())
    }

    fun testDoesNotFlagNonEmptyBlockUsedAsExpressionValue() {
        assertTrue(problems("x = {\ny = 1\ny\n}\n").isEmpty())
    }
}
