package eu.bcosp.vrlintellij.contributors

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLCompletionContributorTest : BasePlatformTestCase() {

    /** Selects [target] from the completion popup, or does nothing if it was already
     * auto-inserted because it was the only match. */
    private fun completeAndAccept(target: String) {
        val elements = myFixture.completeBasic() ?: return
        val match = elements.firstOrNull { it.lookupString == target }
        assertNotNull("expected '$target' among completions: ${elements.map { it.lookupString }}", match)
        myFixture.lookup.currentItem = match
        myFixture.type('\n')
    }

    fun testInsertsParensWithCaretInside() {
        myFixture.configureByText("t.vrl", "upcas<caret>")
        myFixture.completeBasic() // unique match -> auto-inserted, no lookup left open
        assertEquals("upcase()", myFixture.file.text.trim())
        assertEquals(myFixture.file.text.indexOf('(') + 1, myFixture.editor.caretModel.offset)
    }

    fun testDoesNotDuplicateParensWhenAlreadyPresent() {
        myFixture.configureByText("t.vrl", "upcas<caret>(\"x\")")
        myFixture.completeBasic()
        assertEquals("upcase(\"x\")", myFixture.file.text)
        assertEquals(myFixture.file.text.indexOf('(') + 1, myFixture.editor.caretModel.offset)
    }

    fun testSuggestsVariablesInScope() {
        myFixture.configureByText("t.vrl", "count = 1;\nx = cou<caret>")
        completeAndAccept("count")
        assertTrue(myFixture.file.text.contains("x = count"))
    }

    fun testDoesNotSuggestVariableAssignedAfterTheCaret() {
        myFixture.configureByText("t.vrl", "x = cou<caret>\ncount = 1;")
        val lookupStrings = myFixture.completeBasic()?.map { it.lookupString } ?: emptyList()
        assertFalse(lookupStrings.contains("count"))
    }

    fun testSuggestsClosureParamInsideClosureBody() {
        myFixture.configureByText("t.vrl", "map_values(.) -> |item| { x = ite<caret> }")
        completeAndAccept("item")
        assertTrue(myFixture.file.text.contains("x = item"))
    }

    fun testSuggestsArgumentNames() {
        myFixture.configureByText("t.vrl", "split(\"a,b\", <caret>)")
        val lookupStrings = myFixture.completeBasic()?.map { it.lookupString } ?: emptyList()
        assertTrue(lookupStrings.any { it.startsWith("pattern") })
    }

    fun testDoesNotSuggestArgumentNameAlreadyUsedInTheCall() {
        myFixture.configureByText("t.vrl", "split(\"a,b\", pattern: \",\", <caret>)")
        val lookupStrings = myFixture.completeBasic()?.map { it.lookupString } ?: emptyList()
        assertFalse(lookupStrings.any { it.startsWith("pattern") })
    }

    fun testNoSuggestionsInsideStringLiteral() {
        myFixture.configureByText("t.vrl", "x = \"ups<caret>\"")
        val elements = myFixture.completeBasic()
        assertTrue(elements == null || elements.none { it.lookupString == "upcase" })
    }

    fun testNoSuggestionsInsideComment() {
        myFixture.configureByText("t.vrl", "# ups<caret>")
        val elements = myFixture.completeBasic()
        assertTrue(elements == null || elements.none { it.lookupString == "upcase" })
    }

    fun testNoFunctionSuggestionsAfterDot() {
        myFixture.configureByText("t.vrl", ".ups<caret>")
        val elements = myFixture.completeBasic()
        assertTrue(elements == null || elements.none { it.lookupString == "upcase" })
    }

    fun testNoFunctionSuggestionsAfterMemberDot() {
        myFixture.configureByText("t.vrl", "x = 1;\nx.ups<caret>")
        val elements = myFixture.completeBasic()
        assertTrue(elements == null || elements.none { it.lookupString == "upcase" })
    }
}
