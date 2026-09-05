package eu.bcosp.vrlintellij.actions

import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.ui.InputValidator
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.Consumer
import java.awt.Component
import javax.swing.Icon

// Note: this deliberately doesn't test that FileTemplateManager.getInternalTemplate("VRL File")
// resolves - this plugin's `test` task runs against loose class/resource directories rather than
// a packed sandbox plugin jar, and FileTemplatesLoader's internal-template resource enumeration
// only finds entries inside an actual jar, so that call throws "Template not found" here
// regardless of correct registration. The real registration (plugin.xml + the resource living at
// fileTemplates/internal/VRL File.vrl) was verified via a full `buildPlugin` run, whose
// buildSearchableOptions step exercises AllFileTemplatesConfigurable and previously failed with
// exactly that "Template not found" error until the resource was moved into fileTemplates/internal/.
class VRLCreateFileActionTest : BasePlatformTestCase() {

    private val action = VRLCreateFileAction()

    fun `test getActionName mentions the new file's name`() {
        val directoryFile = myFixture.tempDirFixture.findOrCreateDir(".")
        val directory = com.intellij.psi.PsiManager.getInstance(project).findDirectory(directoryFile)!!
        assertTrue(action.getActionName(directory, "foo", "VRL File").contains("foo"))
    }

    fun `test buildDialog registers the VRL File kind`() {
        val directoryFile = myFixture.tempDirFixture.findOrCreateDir(".")
        val directory = com.intellij.psi.PsiManager.getInstance(project).findDirectory(directoryFile)!!

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

        action.buildDialog(project, directory, builder)
        assertEquals("VRL File", registeredKind)
    }

}
