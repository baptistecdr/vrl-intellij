package eu.bcosp.vrlintellij.playground

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.panels.NonOpaquePanel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JComponent

class VRLPlaygroundPanel(private val project: Project) : SimpleToolWindowPanel(true, true), Disposable {

    private var targetFile: VirtualFile? = null

    private val headerLabel = JBLabel().apply {
        border = JBUI.Borders.empty(4, 8)
    }

    private val sampleEventArea = JBTextArea().apply {
        font = Font(Font.MONOSPACED, Font.PLAIN, font.size)
        lineWrap = true
        emptyText.text = "{} (leave empty for an empty event object)"
    }

    private val outputArea = JBTextArea().apply {
        font = Font(Font.MONOSPACED, Font.PLAIN, font.size)
        isEditable = false
        lineWrap = true
        emptyText.text = "Run the script to see its output here."
    }

    private val statusLabel = JBLabel().apply {
        border = JBUI.Borders.empty(4, 8)
    }

    private val runAction = RunAction()

    init {
        val splitter = JBSplitter(true, 0.4f).apply {
            firstComponent = labeled("Sample event", sampleEventArea)
            secondComponent = labeled("Output", outputArea, statusLabel)
        }

        val content = NonOpaquePanel(BorderLayout()).apply {
            add(headerLabel, BorderLayout.NORTH)
            add(splitter, BorderLayout.CENTER)
        }

        toolbar = buildToolbar().component
        setContent(content)

        registerRunShortcut()
        subscribeToFileEditorChanges()
        selectInitialFile()
    }

    private fun labeled(title: String, area: JBTextArea, trailing: JComponent? = null): JComponent {
        val panel = NonOpaquePanel(BorderLayout())
        val label = JBLabel(title).apply { border = JBUI.Borders.empty(4, 8) }
        val north = if (trailing != null) {
            NonOpaquePanel(BorderLayout()).apply {
                add(label, BorderLayout.WEST)
                add(trailing, BorderLayout.EAST)
            }
        } else {
            label
        }
        panel.add(north, BorderLayout.NORTH)
        panel.add(JBScrollPane(area), BorderLayout.CENTER)
        return panel
    }

    private fun buildToolbar(): ActionToolbar {
        val group = DefaultActionGroup(runAction)
        val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true)
        toolbar.targetComponent = this
        return toolbar
    }

    private fun registerRunShortcut() {
        runAction.registerCustomShortcutSet(CommonShortcuts.CTRL_ENTER, sampleEventArea)
    }

    private fun subscribeToFileEditorChanges() {
        project.messageBus.connect(this).subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) {
                    val file = event.newFile ?: return
                    if (file.extension == "vrl") switchTo(file)
                }
            },
        )
    }

    private fun selectInitialFile() {
        val current = FileEditorManager.getInstance(project).selectedFiles.firstOrNull { it.extension == "vrl" }
        if (current != null) switchTo(current) else updateHeader()
    }

    private fun switchTo(file: VirtualFile) {
        if (file == targetFile) return
        persistCurrentSampleEvent()
        targetFile = file
        sampleEventArea.text = VRLPlaygroundState.getInstance(project).sampleEventFor(file.url)
        updateHeader()
    }

    private fun persistCurrentSampleEvent() {
        val file = targetFile ?: return
        VRLPlaygroundState.getInstance(project).setSampleEventFor(file.url, sampleEventArea.text)
    }

    private fun updateHeader() {
        headerLabel.text = targetFile?.let { "Testing: ${it.name}" } ?: "Open a .vrl file to run it against a sample event."
    }

    private fun runPlayground() {
        persistCurrentSampleEvent()
        val file = targetFile
        if (file == null) {
            setStatus("No .vrl file open", isError = true)
            outputArea.text = ""
            return
        }
        val document = FileDocumentManager.getInstance().getDocument(file)
        if (document == null) {
            setStatus("Couldn't read the script's contents", isError = true)
            return
        }
        val program = document.text
        val sampleEvent = sampleEventArea.text
        val vectorBinaryPath = VRLPlaygroundSettings.getInstance().vectorBinaryPath

        setStatus("Running…", isError = false)
        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Running VRL script", true) {
                override fun run(indicator: ProgressIndicator) {
                    val result = VRLPlaygroundRunner.run(vectorBinaryPath, program, sampleEvent)
                    ApplicationManager.getApplication().invokeLater {
                        if (targetFile == file) showResult(result)
                    }
                }
            },
        )
    }

    private fun showResult(result: VRLPlaygroundResult) {
        when (result) {
            is VRLPlaygroundResult.Success -> {
                setStatus("Success", isError = false)
                outputArea.text = buildString {
                    append("── Result ──\n")
                    append(VRLJsonFormatter.prettyPrint(result.expressionResult))
                    append("\n\n── Event ──\n")
                    append(VRLJsonFormatter.prettyPrint(result.mutatedEvent))
                }
                outputArea.caretPosition = 0
            }

            is VRLPlaygroundResult.Failure -> {
                setStatus("Error", isError = true)
                outputArea.text = result.message
                outputArea.caretPosition = 0
            }
        }
    }

    private fun setStatus(text: String, isError: Boolean) {
        statusLabel.text = text
        statusLabel.foreground = if (isError) JBColor.RED else JBColor.foreground()
    }

    override fun dispose() {
        persistCurrentSampleEvent()
    }

    private inner class RunAction : AnAction("Run", "Run the script against the sample event", AllIcons.Actions.Execute) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
        override fun actionPerformed(e: AnActionEvent) = runPlayground()
    }
}
