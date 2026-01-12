package eu.bcosp.vrlintellij.references

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLRenameTest : BasePlatformTestCase() {

    fun testRenamingAVariableFromAUsageUpdatesTheDeclarationAndAllUsages() {
        myFixture.configureByText(
            "t.vrl",
            "count = 1;\nx = co<caret>unt;\ny = count;\n"
        )
        myFixture.renameElementAtCaret("total")
        assertEquals("total = 1;\nx = total;\ny = total;\n", myFixture.file.text)
    }

    fun testRenamingFromTheDeclarationUpdatesAllUsages() {
        myFixture.configureByText(
            "t.vrl",
            "co<caret>unt = 1;\nx = count;\n"
        )
        myFixture.renameElementAtCaret("total")
        assertEquals("total = 1;\nx = total;\n", myFixture.file.text)
    }

    fun testRenamingDoesNotTouchAnUnrelatedVariableOfTheSameNameElsewhere() {
        myFixture.configureByText(
            "t.vrl",
            "count = 1;\nx = co<caret>unt;\ncount = 2;\ny = count;\n"
        )
        myFixture.renameElementAtCaret("total")
        // Renaming the usage bound to the *first* `count` must not touch the second declaration
        // (a separate, later reassignment) or its own usage.
        assertEquals("total = 1;\nx = total;\ncount = 2;\ny = count;\n", myFixture.file.text)
    }

    fun testRenamingAClosureParamUpdatesUsagesInsideTheClosureOnly() {
        myFixture.configureByText(
            "t.vrl",
            "value = 1;\nmap_values(.) -> |val<caret>ue| { upcase(value) }\n"
        )
        myFixture.renameElementAtCaret("item")
        assertEquals("value = 1;\nmap_values(.) -> |item| { upcase(item) }\n", myFixture.file.text)
    }
}
