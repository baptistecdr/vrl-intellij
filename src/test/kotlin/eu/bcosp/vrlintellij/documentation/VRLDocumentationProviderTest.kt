package eu.bcosp.vrlintellij.documentation

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLDocumentationProviderTest : BasePlatformTestCase() {

    private fun documentationAt(offset: Int): String? {
        val provider = VRLDocumentationProvider()
        val element = myFixture.file.findElementAt(offset) ?: return null
        val target = provider.getCustomDocumentationElement(myFixture.editor, myFixture.file, element, offset)
            ?: return null
        return provider.generateDoc(target, element)
    }

    fun testGeneratesDocForKnownFunctionCall() {
        myFixture.configureByText("t.vrl", "upcase(\"x\")")
        val doc = documentationAt(myFixture.file.text.indexOf("upcase") + 2)
        assertNotNull(doc)
        assertTrue(doc!!.contains("upcase"))
        assertTrue(doc.contains("Uppercase"))
    }

    fun testNoDocForPlainVariable() {
        myFixture.configureByText("t.vrl", "x = 1;\ny = x;")
        val offset = myFixture.file.text.lastIndexOf("x") + 1
        assertNull(documentationAt(offset))
    }

    fun testNoDocForUnknownFunctionName() {
        myFixture.configureByText("t.vrl", "totallymadeup(\"x\")")
        val offset = myFixture.file.text.indexOf("totallymadeup") + 2
        assertNull(documentationAt(offset))
    }

    fun testQuickNavigateInfoIncludesSignature() {
        myFixture.configureByText("t.vrl", "upcase(\"x\")")
        val offset = myFixture.file.text.indexOf("upcase") + 2
        val element = myFixture.file.findElementAt(offset)!!
        val provider = VRLDocumentationProvider()
        val info = provider.getQuickNavigateInfo(element, element)
        assertNotNull(info)
        assertTrue(info!!.startsWith("upcase("))
    }

    fun testUrlForKnownFunctionCallPointsAtTheFunctionsReferenceAnchor() {
        myFixture.configureByText("t.vrl", "upcase(\"x\")")
        val offset = myFixture.file.text.indexOf("upcase") + 2
        val element = myFixture.file.findElementAt(offset)!!
        val provider = VRLDocumentationProvider()
        val urls = provider.getUrlFor(element, element)
        assertEquals(listOf("https://vector.dev/docs/reference/vrl/functions/#upcase"), urls)
    }

    fun testNoUrlForUnknownFunctionName() {
        myFixture.configureByText("t.vrl", "totallymadeup(\"x\")")
        val offset = myFixture.file.text.indexOf("totallymadeup") + 2
        val element = myFixture.file.findElementAt(offset)!!
        val provider = VRLDocumentationProvider()
        assertNull(provider.getUrlFor(element, element))
    }

    fun testNoUrlForPlainVariable() {
        myFixture.configureByText("t.vrl", "x = 1;\ny = x;")
        val offset = myFixture.file.text.lastIndexOf("x") + 1
        val element = myFixture.file.findElementAt(offset)!!
        val provider = VRLDocumentationProvider()
        assertNull(provider.getUrlFor(element, element))
    }
}
