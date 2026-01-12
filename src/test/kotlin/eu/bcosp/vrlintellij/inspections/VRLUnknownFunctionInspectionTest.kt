package eu.bcosp.vrlintellij.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLUnknownFunctionInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(VRLUnknownFunctionInspection())
    }

    fun testFlagsUnknownFunction() {
        myFixture.configureByText("t.vrl", "totallymadeup(\"x\")")
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("Unknown function") == true })
    }

    fun testDoesNotFlagKnownFunction() {
        myFixture.configureByText("t.vrl", "upcase(\"x\")")
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("Unknown function") == true })
    }

    fun testSuggestsFixForCloseTypo() {
        myFixture.configureByText("t.vrl", "upcse(\"x\")")
        myFixture.doHighlighting()
        val fix = myFixture.getAllQuickFixes().firstOrNull { it.text.contains("upcase") }
        assertNotNull("expected a 'did you mean upcase' quick fix", fix)
        myFixture.launchAction(fix!!)
        assertEquals("upcase(\"x\")", myFixture.file.text)
    }

    fun testNoFixSuggestedForUnrelatedName() {
        myFixture.configureByText("t.vrl", "totallymadeup(\"x\")")
        myFixture.doHighlighting()
        // getAllQuickFixes() also includes the generic "Inspection 'X' options" intention the
        // platform adds to every inspection warning, so only "Change to '...'" fixes count here.
        assertTrue(myFixture.getAllQuickFixes().none { it.text.startsWith("Change to") })
    }
}
