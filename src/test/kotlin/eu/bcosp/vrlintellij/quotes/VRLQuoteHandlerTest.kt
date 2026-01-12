package eu.bcosp.vrlintellij.quotes

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLQuoteHandlerTest : BasePlatformTestCase() {

    fun testTypingOpeningQuoteInsertsClosingQuote() {
        myFixture.configureByText("t.vrl", "x = <caret>")
        myFixture.type('"')
        assertEquals("x = \"\"", myFixture.editor.document.text)
        assertEquals(myFixture.editor.document.text.indexOf('"') + 1, myFixture.editor.caretModel.offset)
    }

    fun testTypingClosingQuoteSkipsOverTheAutoInsertedOne() {
        myFixture.configureByText("t.vrl", "x = <caret>")
        myFixture.type('"')
        myFixture.type('h')
        myFixture.type('i')
        myFixture.type('"')
        val text = myFixture.editor.document.text
        assertEquals("x = \"hi\"", text)
        assertEquals(text.length, myFixture.editor.caretModel.offset)
    }

    fun testUnterminatedStringStillHighlightsWithoutError() {
        // Regression check for the lexer fix this relies on: STRING_LITERAL's closing quote had
        // to become optional so a freshly-typed opening quote lexes as STRING (not
        // BAD_CHARACTER), which is what lets the quote handler recognize it at all.
        myFixture.configureByText("t.vrl", "x = \"hello")
        myFixture.doHighlighting()
    }
}
