package eu.bcosp.vrlintellij.templates

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLLiveTemplatesTest : BasePlatformTestCase() {

    private fun template(name: String): Template =
        TemplateSettings.getInstance().getTemplate(name, "VRL") ?: error("template '$name' not registered")

    private fun expand(templateName: String): String {
        myFixture.configureByText("t.vrl", "")
        WriteCommandAction.runWriteCommandAction(project) {
            TemplateManager.getInstance(project).startTemplate(myFixture.editor, template(templateName))
        }
        return myFixture.file.text
    }

    fun `test if template is registered in the VRL group`() {
        assertNotNull(TemplateSettings.getInstance().getTemplate("if", "VRL"))
    }

    fun `test ifel template is registered in the VRL group`() {
        assertNotNull(TemplateSettings.getInstance().getTemplate("ifel", "VRL"))
    }

    fun `test foreach template is registered in the VRL group`() {
        assertNotNull(TemplateSettings.getInstance().getTemplate("foreach", "VRL"))
    }

    fun `test if template expands to an if block`() {
        assertEquals("if true {\n    \n}", expand("if"))
    }

    fun `test foreach template expands to a for_each closure`() {
        assertEquals("for_each(.) -> |key, value| {\n    \n}", expand("foreach"))
    }

    fun `test only usable in a vrl file`() {
        val contextType = VRLTemplateContextType()
        myFixture.configureByText("t.txt", "")
        val vrlContext = com.intellij.codeInsight.template.TemplateActionContext.create(myFixture.file, myFixture.editor, 0, 0, false)
        assertFalse(contextType.isInContext(vrlContext))
    }
}
