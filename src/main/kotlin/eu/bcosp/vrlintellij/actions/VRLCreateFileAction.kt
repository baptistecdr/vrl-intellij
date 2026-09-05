package eu.bcosp.vrlintellij.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import eu.bcosp.vrlintellij.VRLIcons

/** Adds "VRL File" to File | New..., so a `.vrl` script can be created without leaving the IDE. */
class VRLCreateFileAction : CreateFileFromTemplateAction(TEMPLATE_NAME, "Creates a new VRL file", VRLIcons.FILE) {

    public override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle("New VRL File")
            .addKind(TEMPLATE_NAME, VRLIcons.FILE, TEMPLATE_NAME)
    }

    public override fun getActionName(directory: PsiDirectory, newName: String, templateName: String): String =
        "Create VRL File $newName"

    companion object {
        private const val TEMPLATE_NAME = "VRL File"
    }
}
