package eu.bcosp.vrlintellij.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import eu.bcosp.vrlintellij.VRL
import eu.bcosp.vrlintellij.VRLFileType

class VRLFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, VRL) {
    override fun getFileType(): FileType {
        return VRLFileType
    }

    override fun toString(): String {
        return "VRL File"
    }
}
