package eu.bcosp.vrlintellij.formatting

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

/**
 * Backs the "Use trailing comma" option on VRL's Code Style > Wrapping and Braces page (see
 * [VRLLanguageCodeStyleSettingsProvider]). A plain `@JvmField var`, not a Kotlin property with a
 * generated getter/setter, since the platform's settings persistence (readExternal/writeExternal)
 * reflects over public fields the same way [com.intellij.psi.codeStyle.CommonCodeStyleSettings]'s
 * own SPACE_BEFORE_COMMA-style fields do.
 */
class VRLCodeStyleSettings(container: CodeStyleSettings) : CustomCodeStyleSettings("VRLCodeStyleSettings", container) {
    @JvmField
    var TRAILING_COMMA: Boolean = false
}
