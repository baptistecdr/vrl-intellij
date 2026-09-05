package eu.bcosp.vrlintellij.playground

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent

class VRLPlaygroundConfigurable : Configurable {

    private var binaryPathField: TextFieldWithBrowseButton? = null

    override fun getDisplayName(): String = "VRL Playground"

    override fun createComponent(): JComponent {
        val descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
            .withTitle("Select Vector Executable")
            .withDescription("Choose the vector binary used to run VRL scripts against a sample event.")

        val field = TextFieldWithBrowseButton()
        field.addBrowseFolderListener(null, descriptor)
        binaryPathField = field

        val hint = JBLabel(
            "<html>Path to the <code>vector</code> executable. Leave as \"vector\" to resolve it from PATH.<br>" +
                "Used by the VRL Playground tool window, which runs the current script against a sample event " +
                "via <code>vector vrl</code>.</html>",
        )

        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Vector CLI path:", field)
            .addComponentToRightColumn(hint)
            .panel
    }

    override fun isModified(): Boolean =
        binaryPathField?.text.orEmpty() != VRLPlaygroundSettings.getInstance().vectorBinaryPath

    override fun apply() {
        VRLPlaygroundSettings.getInstance().vectorBinaryPath = binaryPathField?.text.orEmpty().ifBlank { "vector" }
    }

    override fun reset() {
        binaryPathField?.text = VRLPlaygroundSettings.getInstance().vectorBinaryPath
    }
}
