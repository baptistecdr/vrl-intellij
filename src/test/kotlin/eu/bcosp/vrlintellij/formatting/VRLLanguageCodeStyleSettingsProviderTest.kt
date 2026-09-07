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

    // Regression test for a real bug: LanguageCodeStyleSettingsProvider.createConfigurable() is
    // NOT overridden by default - the inherited implementation just throws - and
    // getSettingsPagesProviders() (what Settings | Editor | Code Style actually builds its
    // language list from) reflectively excludes any provider whose createConfigurable() is still
    // that inherited stub, to avoid crashing when it's invoked. Without overriding it ourselves,
    // "VRL" silently never appeared as a language in Code Style settings at all - confirmed by
    // decompiling the platform's own filtering logic, and by cross-checking JSON's and
    // Properties' bundled providers, which both override it the same way.
    fun `test gets its own Code Style settings page`() {
        assertTrue(LanguageCodeStyleSettingsProvider.getSettingsPagesProviders().any { it.language == VRL })
    }

    fun `test createConfigurable does not throw and returns a real panel`() {
        val settings = CodeStyle.getSettings(project)
        val configurable = provider.createConfigurable(settings, settings)
        val component = configurable.createComponent()
        assertNotNull(component)
        configurable.disposeUIResources()
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
