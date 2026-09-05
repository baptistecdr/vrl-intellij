package eu.bcosp.vrlintellij.templates

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

class VRLDefaultLiveTemplatesProvider : DefaultLiveTemplatesProvider {
    override fun getDefaultLiveTemplateFiles(): Array<String> = arrayOf("/liveTemplates/VRL")

    override fun getHiddenLiveTemplateFiles(): Array<String>? = null
}
