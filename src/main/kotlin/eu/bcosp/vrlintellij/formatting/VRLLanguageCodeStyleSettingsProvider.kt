package eu.bcosp.vrlintellij.formatting

import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import eu.bcosp.vrlintellij.VRL

/**
 * Registers VRL on the Settings/Preferences | Editor | Code Style page. Without this, `.vrl`
 * files fall back to the IDE's generic "Other File Types" indent/blank-line settings instead of
 * their own configurable slot - e.g. changing "Tab size"/"Indent" here had no effect at all before
 * this was registered, since [VRLBlock]'s `Indent.getNormalIndent()` resolves against
 * per-language indent options that only exist once a provider like this one is registered.
 */
class VRLLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {

    override fun getLanguage() = VRL

    override fun getCodeSample(settingsType: SettingsType): String = SAMPLE

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        when (settingsType) {
            SettingsType.SPACING_SETTINGS -> consumer.showStandardOptions(
                "SPACE_BEFORE_COMMA",
                "SPACE_AFTER_COMMA",
            )

            SettingsType.BLANK_LINES_SETTINGS -> consumer.showStandardOptions(
                "KEEP_BLANK_LINES_IN_CODE",
            )

            else -> Unit
        }
    }

    companion object {
        private val SAMPLE = """
            parse_json!(.message)
            if .status == 200 {
                .level = "info"
            } else {
                .level = "error"
            }

            .tags = ["a", "b", "c"]
        """.trimIndent()
    }
}
