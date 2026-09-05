package eu.bcosp.vrlintellij.playground

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent

class VRLPlaygroundConfigurable : Configurable {

    private var binaryPathField: TextFieldWithBrowseButton? = null
    private var externalDiagnosticsCheckBox: JBCheckBox? = null

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
                "Used by the VRL Playground tool window (run the current script against a sample event) and, " +
                "if enabled below, for live compiler diagnostics.</html>",
        )

        val checkBox = JBCheckBox("Show diagnostics from the real vector compiler while editing")
        externalDiagnosticsCheckBox = checkBox
        val diagnosticsHint = JBLabel(
            "<html>Off by default. Runs <code>vector vrl</code> in the background on every edit and " +
                "annotates errors it reports - a ground-truth check on top of this plugin's own inspections. " +
                "Silently does nothing if the vector executable above can't be found.</html>",
        )

        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Vector CLI path:", field)
            .addComponentToRightColumn(hint)
            .addComponent(checkBox)
            .addComponentToRightColumn(diagnosticsHint)
            .panel
    }

    override fun isModified(): Boolean {
        val settings = VRLPlaygroundSettings.getInstance()
        return binaryPathField?.text.orEmpty() != settings.vectorBinaryPath ||
            externalDiagnosticsCheckBox?.isSelected != settings.externalDiagnosticsEnabled
    }

    override fun apply() {
        val settings = VRLPlaygroundSettings.getInstance()
        settings.vectorBinaryPath = binaryPathField?.text.orEmpty().ifBlank { "vector" }
        settings.externalDiagnosticsEnabled = externalDiagnosticsCheckBox?.isSelected ?: false
    }

    override fun reset() {
        val settings = VRLPlaygroundSettings.getInstance()
        binaryPathField?.text = settings.vectorBinaryPath
        externalDiagnosticsCheckBox?.isSelected = settings.externalDiagnosticsEnabled
    }
}
