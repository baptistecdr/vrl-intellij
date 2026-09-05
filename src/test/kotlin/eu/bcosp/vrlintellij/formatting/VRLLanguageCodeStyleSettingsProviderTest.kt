package eu.bcosp.vrlintellij.formatting

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import eu.bcosp.vrlintellij.VRL

class VRLLanguageCodeStyleSettingsProviderTest : BasePlatformTestCase() {

    private val provider = VRLLanguageCodeStyleSettingsProvider()

    fun `test registers VRL as its language`() {
        assertEquals(VRL, provider.language)
    }

    fun `test provides a non-blank code sample`() {
        for (settingsType in LanguageCodeStyleSettingsProvider.SettingsType.entries) {
            assertTrue(provider.getCodeSample(settingsType).isNotBlank())
        }
    }

    private fun commonSettings(): CommonCodeStyleSettings = CodeStyle.getSettings(project).getCommonSettings(VRL)

    private fun reformat(text: String): String {
        myFixture.configureByText("t.vrl", text)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(myFixture.file)
        }
        return myFixture.file.text
    }

    private fun <T> withSetting(get: () -> T, set: (T) -> Unit, value: T, block: () -> Unit) {
        val original = get()
        set(value)
        try {
            block()
        } finally {
            set(original)
        }
    }

    fun `test a configured indent size is respected`() {
        val indentOptions = commonSettings().indentOptions!!
        withSetting({ indentOptions.INDENT_SIZE }, { indentOptions.INDENT_SIZE = it }, 2) {
            val result = reformat("if true {\ncount = 1;\n}\n")
            assertEquals("if true {\n  count = 1;\n}\n", result)
        }
    }

    fun `test KEEP_BLANK_LINES_IN_CODE of zero collapses blank lines`() {
        val settings = commonSettings()
        withSetting({ settings.KEEP_BLANK_LINES_IN_CODE }, { settings.KEEP_BLANK_LINES_IN_CODE = it }, 0) {
            val result = reformat("x = 1;\n\n\ny = 2;\n")
            assertEquals("x = 1;\ny = 2;\n", result)
        }
    }

    fun `test SPACE_BEFORE_COMMA is respected`() {
        val settings = commonSettings()
        withSetting({ settings.SPACE_BEFORE_COMMA }, { settings.SPACE_BEFORE_COMMA = it }, true) {
            val result = reformat("x = [1,2];\n")
            assertEquals("x = [1 , 2];\n", result)
        }
    }

    fun `test SPACE_AFTER_COMMA disabled removes the space`() {
        val settings = commonSettings()
        withSetting({ settings.SPACE_AFTER_COMMA }, { settings.SPACE_AFTER_COMMA = it }, false) {
            val result = reformat("x = [1,2];\n")
            assertEquals("x = [1,2];\n", result)
        }
    }
}
