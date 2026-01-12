package eu.bcosp.vrlintellij

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import eu.bcosp.vrlintellij.VRL.VRL_MIME_TYPE


object VRL : Language("VRL", VRL_MIME_TYPE) {
    const val VRL_MIME_TYPE: String = "application/vrl"

    private fun readResolve(): Any = VRL

    override fun getAssociatedFileType(): LanguageFileType {
        return VRLFileType
    }
}
