package eu.bcosp.vrlintellij

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import javax.swing.Icon
import org.jetbrains.annotations.NonNls

object VRLFileType : LanguageFileType(VRL) {
    const val DEFAULT_EXTENSION: String = "vrl"

    override fun getName(): @NonNls String {
        return "VRL"
    }

    override fun getDescription(): @NlsContexts.Label String {
        return "VRL language file"
    }

    override fun getDefaultExtension(): @NlsSafe String {
        return DEFAULT_EXTENSION
    }

    override fun getIcon(): Icon {
        return VRLIcons.FILE
    }
}
