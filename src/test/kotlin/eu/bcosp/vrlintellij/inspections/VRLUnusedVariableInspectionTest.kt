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

    // `_ = 1` is a compile error in VRL (E640 "unnecessary no-op assignment") - `_` is only legal
    // as a multi-assignment target, so a single `x = ...` assignment's fix has to be different
    // from a `value, err = ...` one's (see testQuickFixRenamesMultiAssignmentTargetToUnderscore).
    fun testQuickFixRemovesUnusedSingleAssignment() {
        myFixture.configureByText("t.vrl", "x = 1\n")
        val intention = myFixture.getAvailableIntention("Remove unused assignment")
        assertNotNull(intention)
        assertNull(myFixture.getAvailableIntention("Rename to '_'"))
        myFixture.launchAction(intention!!)
        myFixture.checkResult("1\n")
    }

    fun testQuickFixRemovesUnusedSingleAssignmentKeepsCallSideEffect() {
        myFixture.configureByText("t.vrl", "x = log(\"hi\")\n")
        val intention = myFixture.getAvailableIntention("Remove unused assignment")
        assertNotNull(intention)
        myFixture.launchAction(intention!!)
        myFixture.checkResult("log(\"hi\")\n")
    }

    fun testQuickFixRenamesMultiAssignmentTargetToUnderscore() {
        myFixture.configureByText("t.vrl", "value, err = parse_json(.message)\n")
        val intention = myFixture.getAvailableIntention("Rename to '_'")
        assertNotNull(intention)
        assertNull(myFixture.getAvailableIntention("Remove unused assignment"))
        myFixture.launchAction(intention!!)
        myFixture.checkResult("_, err = parse_json(.message)\n")
    }
}
