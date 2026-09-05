package eu.bcosp.vrlintellij.playground

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class VRLPlaygroundConfigurable : Configurable {

    private var settingsPanel: DialogPanel? = null

    override fun getDisplayName(): String = "VRL Playground"

    override fun createComponent(): JComponent {
        val settings = VRLPlaygroundSettings.getInstance()
        val descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
            .withTitle("Select Vector Executable")
            .withDescription("Choose the vector binary used to run VRL scripts against a sample event.")

        val newPanel = panel {
            row("Vector CLI path:") {
                val field = TextFieldWithBrowseButton()
                field.addBrowseFolderListener(null, descriptor)
                cell(field)
                    .align(AlignX.FILL)
                    .bindText(
                        { settings.vectorBinaryPath },
                        { settings.vectorBinaryPath = it.ifBlank { "vector" } },
                    )
                    .comment(
                        "Path to the <code>vector</code> executable. Leave as \"vector\" to resolve it from " +
                            "PATH. Used by the VRL Playground tool window (run the current script against a " +
                            "sample event) and, if enabled below, for live compiler diagnostics.",
                    )
            }
            row {
                checkBox("Show diagnostics from the real vector compiler while editing")
                    .bindSelected(
                        { settings.externalDiagnosticsEnabled },
                        { settings.externalDiagnosticsEnabled = it },
                    )
                    .comment(
                        "Off by default. Runs <code>vector vrl</code> in the background on every edit and " +
                            "annotates errors it reports - a ground-truth check on top of this plugin's own " +
                            "inspections. Silently does nothing if the vector executable above can't be found.",
                    )
            }
        }
        settingsPanel = newPanel
        return newPanel
    }

    override fun isModified(): Boolean = settingsPanel?.isModified() ?: false

    override fun apply() {
        settingsPanel?.apply()
    }

    override fun reset() {
        settingsPanel?.reset()
    }
}
