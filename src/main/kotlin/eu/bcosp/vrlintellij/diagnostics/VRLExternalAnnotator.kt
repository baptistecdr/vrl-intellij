package eu.bcosp.vrlintellij.diagnostics

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import eu.bcosp.vrlintellij.playground.VRLPlaygroundSettings

data class VRLDiagnosticsInfo(val programText: String, val vectorBinaryPath: String)

data class VRLDiagnosticsResult(val diagnostics: List<VRLCompilerDiagnostic>)

/**
 * Cross-checks the current VRL file against Vector's own real compiler (`vector vrl`), surfacing
 * whatever it reports as editor error annotations - a ground-truth check on top of this plugin's
 * own hand-written inspections, which only approximate VRL's real (flow-sensitive) type system.
 * Every diagnostic `vector vrl` prints to stderr corresponds to a program that failed to compile
 * (there's no separate non-fatal "warning" diagnostic kind - see [VRLCompilerDiagnosticsParser]),
 * so every one is reported at [HighlightSeverity.ERROR].
 *
 * Controlled by [VRLPlaygroundSettings.externalDiagnosticsEnabled] and fails silently (no
 * annotations, no error banner) whenever the configured `vector` binary can't be run - a plugin
 * feature shouldn't nag every user who doesn't have Vector installed.
 */
class VRLExternalAnnotator : ExternalAnnotator<VRLDiagnosticsInfo, VRLDiagnosticsResult>() {

    override fun collectInformation(file: PsiFile): VRLDiagnosticsInfo? {
        val settings = VRLPlaygroundSettings.getInstance()
        if (!settings.externalDiagnosticsEnabled) return null
        // An injected VRL fragment (inside a TOML/YAML pipeline config) has its own offsets and
        // is usually not a complete, independently-compilable program on its own - only run this
        // against real top-level .vrl files.
        if (InjectedLanguageManager.getInstance(file.project).isInjectedFragment(file)) return null
        val vectorBinaryPath = settings.vectorBinaryPath
        if (vectorBinaryPath.isBlank()) return null
        return VRLDiagnosticsInfo(file.text, vectorBinaryPath)
    }

    override fun doAnnotate(collectedInfo: VRLDiagnosticsInfo): VRLDiagnosticsResult? {
        val stderr = VRLCompilerDiagnosticsRunner.run(collectedInfo.vectorBinaryPath, collectedInfo.programText)
            ?: return null
        return VRLDiagnosticsResult(VRLCompilerDiagnosticsParser.parse(stderr))
    }

    override fun apply(file: PsiFile, annotationResult: VRLDiagnosticsResult?, holder: AnnotationHolder) {
        if (annotationResult == null) return
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return
        for (diagnostic in annotationResult.diagnostics) {
            val range = rangeFor(file, document, diagnostic) ?: continue
            holder.newAnnotation(HighlightSeverity.ERROR, formatMessage(diagnostic)).range(range).create()
        }
    }

    private fun formatMessage(diagnostic: VRLCompilerDiagnostic): String = buildString {
        append("vector: ")
        append(diagnostic.title)
        if (diagnostic.detail.isNotBlank()) {
            append(" - ")
            append(diagnostic.detail)
        }
        append(" [")
        append(diagnostic.code)
        append(']')
    }

    /**
     * Maps the compiler's 1-indexed (line, column) to a document range by finding the PSI leaf at
     * that offset and highlighting its real text range - deliberately not the exact span width
     * `vector vrl`'s own underline (`^^^`) draws in the terminal, which would need parsing
     * variable-width ASCII art to reproduce. Anchoring to a real PSI token instead is simpler,
     * always a valid range, and gives a highlight shaped the same way this plugin's own
     * inspections already shape theirs.
     */
    private fun rangeFor(file: PsiFile, document: Document, diagnostic: VRLCompilerDiagnostic): TextRange? {
        val lineIndex = diagnostic.line - 1
        if (lineIndex !in 0 until document.lineCount) return null
        val lineStart = document.getLineStartOffset(lineIndex)
        val lineEnd = document.getLineEndOffset(lineIndex)
        val offset = (lineStart + diagnostic.column - 1).coerceIn(lineStart, lineEnd)

        val element = file.findElementAt(offset)?.takeIf { it.textRange.length > 0 }
        if (element != null) return element.textRange

        val safeEnd = minOf(offset + 1, document.textLength)
        val safeStart = minOf(offset, safeEnd)
        return if (safeStart < safeEnd) TextRange(safeStart, safeEnd) else null
    }
}
