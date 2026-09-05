package eu.bcosp.vrlintellij.spellchecking

import com.intellij.spellchecker.inspections.SpellCheckingInspection
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLSpellcheckingStrategyTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(SpellCheckingInspection())
    }

    fun `test flags a misspelled word inside a string literal`() {
        myFixture.configureByText("t.vrl", "x = \"helllo wrold\";\n")
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("Typo") == true })
    }

    fun `test flags a misspelled word inside a comment`() {
        myFixture.configureByText("t.vrl", "# thsi is a coment\nx = 1;\n")
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("Typo") == true })
    }

    fun `test does not flag a keyword as misspelled`() {
        myFixture.configureByText("t.vrl", "if true {\nx = 1;\n}\n")
        val highlights = myFixture.doHighlighting()
        assertFalse(highlights.any { it.description?.contains("Typo") == true })
    }
}
