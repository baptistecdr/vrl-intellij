package eu.bcosp.vrlintellij.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLUnusedVariableInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(VRLUnusedVariableInspection())
    }

    private fun problems(text: String): List<String> {
        myFixture.configureByText("t.vrl", text)
        return myFixture.doHighlighting().mapNotNull { it.description }
    }

    fun testFlagsAssignmentNeverRead() {
        val messages = problems("x = 1\n")
        assertTrue(messages.any { it.contains("'x' is never used") })
    }

    fun testDoesNotFlagVariableReadAfterwards() {
        val messages = problems("x = 1\ny = x\n")
        assertTrue(messages.none { it.contains("'x' is never used") })
        assertTrue(messages.any { it.contains("'y' is never used") })
    }

    fun testDoesNotFlagUnderscore() {
        assertTrue(problems("_ = 1\n").isEmpty())
    }

    fun testFlagsBothUnusedTargetsOfMultiAssignment() {
        val messages = problems("value, err = parse_json(.message)\n")
        assertTrue(messages.any { it.contains("'value' is never used") })
        assertTrue(messages.any { it.contains("'err' is never used") })
    }

    fun testDoesNotFlagMultiAssignmentTargetThatIsRead() {
        val messages = problems("value, err = parse_json(.message)\nif err != null {\n}\n.result = value\n")
        assertTrue(messages.none { it.contains("is never used") })
    }

    fun testDoesNotFlagVariableUsedInsideClosure() {
        val messages = problems("x = 1\nmap_values(.) -> |v| {\ny = x\n}\n")
        assertTrue(messages.none { it.contains("'x' is never used") })
    }

    fun testDoesNotFlagClosureParameters() {
        assertTrue(problems("map_values(.) -> |v| {\n1\n}\n").isEmpty())
    }

    fun testDoesNotFlagPathAssignment() {
        assertTrue(problems(".message = 1\n").isEmpty())
    }

    fun testOnlyEarlierAssignmentIsFlaggedWhenReassignedBeforeUse() {
        val messages = problems("x = 1\nx = 2\ny = x\n")
        assertEquals(1, messages.count { it.contains("'x' is never used") })
    }

    fun testQuickFixRenamesToUnderscore() {
        myFixture.configureByText("t.vrl", "x = 1\n")
        val intention = myFixture.getAvailableIntention("Rename to '_'")
        assertNotNull(intention)
        myFixture.launchAction(intention!!)
        myFixture.checkResult("_ = 1\n")
    }
}
