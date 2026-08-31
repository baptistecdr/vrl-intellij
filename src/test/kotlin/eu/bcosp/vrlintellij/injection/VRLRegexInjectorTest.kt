package eu.bcosp.vrlintellij.injection

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.lang.regexp.RegExpLanguage

class VRLRegexInjectorTest : BasePlatformTestCase() {

    private fun injectedElementAt(text: String, needle: String) = run {
        myFixture.configureByText("t.vrl", text)
        val offset = myFixture.file.text.indexOf(needle)
        InjectedLanguageManager.getInstance(myFixture.project).findInjectedElementAt(myFixture.file, offset)
    }

    fun testRegexLiteralHasRegExpLanguageInjected() {
        val injected = injectedElementAt("x = r'^foo\$'\n", "foo")
        assertNotNull(injected)
        assertEquals(RegExpLanguage.INSTANCE, injected!!.containingFile.language)
    }

    fun testInjectedContentExcludesDelimiters() {
        val injected = injectedElementAt("x = r'^foo\$'\n", "foo")
        assertEquals("^foo\$", injected!!.containingFile.text)
    }

    fun testStringLiteralHasNoAutomaticRegexInjection() {
        assertNull(injectedElementAt("x = \"foo\"\n", "foo"))
    }

    fun testRawStringLiteralHasNoAutomaticRegexInjection() {
        assertNull(injectedElementAt("x = s'foo'\n", "foo"))
    }
}
