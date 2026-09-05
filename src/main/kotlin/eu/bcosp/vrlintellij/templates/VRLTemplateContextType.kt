package eu.bcosp.vrlintellij.templates

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import eu.bcosp.vrlintellij.VRL

class VRLTemplateContextType : TemplateContextType("VRL") {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean =
        templateActionContext.file.language.isKindOf(VRL)
}
