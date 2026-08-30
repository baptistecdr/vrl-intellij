package eu.bcosp.vrlintellij.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLUnresolvedVariableInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(VRLUnresolvedVariableInspection())
    }

    private fun problems(text: String): List<String> {
        myFixture.configureByText("t.vrl", text)
        return myFixture.doHighlighting().mapNotNull { it.description }
    }

    fun testFlagsReadOfNeverAssignedVariable() {
        val messages = problems("x = y\n")
        assertTrue(messages.any { it.contains("Unresolved variable 'y'") })
    }

    fun testDoesNotFlagAssignmentTarget() {
        assertTrue(problems("x = 1\n").isEmpty())
    }

    fun testDoesNotFlagVariableAssignedBeforeUse() {
        assertTrue(problems("x = 1\ny = x\n").isEmpty())
    }

    fun testDoesNotFlagClosureParameterUse() {
        assertTrue(problems("map_values(.) -> |v| {\nx = v\n}\n").isEmpty())
    }

    fun testDoesNotFlagPathExpression() {
        assertTrue(problems(".foo = 1\nx = .foo\n").isEmpty())
    }

    fun testDoesNotFlagFunctionCallName() {
        assertTrue(problems("upcase(\"x\")\n").isEmpty())
    }

    fun testSuggestsCloseVisibleVariableName() {
        val messages = problems("value = 1\ny = vlaue\n")
        assertTrue(messages.any { it.contains("Unresolved variable 'vlaue'") && it.contains("Did you mean 'value'") })
    }

    fun testDoesNotSuggestWhenNothingIsClose() {
        val messages = problems("value = 1\ny = zzzzzzzzzz\n")
        assertTrue(messages.any { it.contains("Unresolved variable 'zzzzzzzzzz'") && !it.contains("Did you mean") })
    }

    fun testQuickFixAppliesSuggestion() {
        myFixture.configureByText("t.vrl", "value = 1\ny = <caret>vlaue\n")
        val intention = myFixture.getAvailableIntention("Change to 'value'")
        assertNotNull(intention)
        myFixture.launchAction(intention!!)
        myFixture.checkResult("value = 1\ny = value\n")
    }
}
