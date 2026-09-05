package eu.bcosp.vrlintellij.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.InputValidator
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.Consumer
import java.awt.Component
import javax.swing.Icon

class VRLCreateFileActionTest : BasePlatformTestCase() {

    private val action = VRLCreateFileAction()

    private fun directory() = PsiManager.getInstance(project).findDirectory(myFixture.tempDirFixture.findOrCreateDir("."))!!

    // Exercises the exact path CreateFileFromTemplateAction.createFile() uses at runtime
    // (FileTemplateManager.getInstance(project).getInternalTemplate("VRL File")). This is a
    // regression test for a real bug: the template resource must be named
    // "VRL File.vrl.ft" (extension + ".ft" suffix, matching every bundled template - e.g.
    // fileTemplates/internal/Less File.less.ft), not "VRL File.vrl" - the missing ".ft" made this
    // lookup throw "Template not found: VRL File" when actually creating a file via File > New.
    fun `test the internal file template resolves and produces a working vrl file`() {
        val template = FileTemplateManager.getInstance(project).getInternalTemplate("VRL File")
        assertNotNull(template)

        val created = WriteCommandAction.runWriteCommandAction<com.intellij.psi.PsiFile>(project) {
            CreateFileFromTemplateAction.createFileFromTemplate("MyScript", template, directory(), null, true)
        }
        assertEquals("MyScript.vrl", created.name)
    }

    fun `test getActionName mentions the new file's name`() {
        assertTrue(action.getActionName(directory(), "foo", "VRL File").contains("foo"))
    }

    fun `test buildDialog registers the VRL File kind`() {
        var registeredKind: String? = null
        val builder = object : CreateFileFromTemplateDialog.Builder {
            override fun setTitle(title: String) = this
            override fun setValidator(validator: InputValidator?) = this
            override fun setDefaultText(text: String?) = this
            override fun setDialogOwner(component: Component?) = this
            override fun addKind(kind: String, icon: Icon?, templateName: String, validator: InputValidator?): CreateFileFromTemplateDialog.Builder {
                registeredKind = kind
                return this
            }

            override fun <T : PsiElement> show(
                errorTitle: String,
                selectedTemplateName: String?,
                creator: CreateFileFromTemplateDialog.FileCreator<T>,
            ): T? = null

            override fun <T : PsiElement> show(
                errorTitle: String,
                selectedTemplateName: String?,
                creator: CreateFileFromTemplateDialog.FileCreator<T>,
                consumer: Consumer<in T>,
            ) = Unit

            override fun getCustomProperties(): MutableMap<String, String> = mutableMapOf()
        }

        action.buildDialog(project, directory(), builder)
        assertEquals("VRL File", registeredKind)
    }
}
