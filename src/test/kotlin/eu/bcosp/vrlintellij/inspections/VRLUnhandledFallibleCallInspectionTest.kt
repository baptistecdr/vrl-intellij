package eu.bcosp.vrlintellij.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLUnhandledFallibleCallInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(VRLUnhandledFallibleCallInspection())
    }

    private fun hasUnhandledFallibleWarning(): Boolean =
        myFixture.doHighlighting().any { it.description?.contains("Unhandled fallible function call") == true }

    fun testFlagsPlainAssignmentOfFallibleCall() {
        myFixture.configureByText("t.vrl", "x = parse_json(.message)\n")
        assertTrue(hasUnhandledFallibleWarning())
    }

    fun testFlagsFallibleCallUsedAsArgument() {
        myFixture.configureByText("t.vrl", "upcase(parse_json(.message))\n")
        assertTrue(hasUnhandledFallibleWarning())
    }

    fun testDoesNotFlagInfallibleFunction() {
        myFixture.configureByText("t.vrl", "x = upcase(\"a\")\n")
        assertFalse(hasUnhandledFallibleWarning())
    }

    fun testDoesNotFlagRaiseSuffix() {
        myFixture.configureByText("t.vrl", "x = parse_json!(.message)\n")
        assertFalse(hasUnhandledFallibleWarning())
    }

    fun testDoesNotFlagMultiTargetErrorAssignment() {
        myFixture.configureByText("t.vrl", "x, err = parse_json(.message)\n")
        assertFalse(hasUnhandledFallibleWarning())
    }

    fun testDoesNotFlagNullCoalesceFallback() {
        myFixture.configureByText("t.vrl", "x = parse_json(.message) ?? {}\n")
        assertFalse(hasUnhandledFallibleWarning())
    }

    fun testQuickFixInsertsRaiseFlag() {
        myFixture.configureByText("t.vrl", "x = parse_json(.message)\n")
        myFixture.doHighlighting()
        val fix = myFixture.getAllQuickFixes().firstOrNull { it.text.contains("raise on error") }
        assertNotNull("expected an 'add raise flag' quick fix", fix)
        myFixture.launchAction(fix!!)
        assertEquals("x = parse_json!(.message)\n", myFixture.file.text)
    }
}
