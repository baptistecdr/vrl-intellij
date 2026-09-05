package eu.bcosp.vrlintellij.diagnostics

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessNotCreatedException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * Runs a VRL program through `vector vrl`'s compile step (https://vector.dev/docs/reference/cli/#vrl)
 * and returns the raw stderr for [VRLCompilerDiagnosticsParser] to parse, or null if the binary
 * couldn't be run at all (not installed, wrong configured path, timeout) - callers should fail
 * silently in that case rather than annotate every VRL file with a persistent error.
 *
 * An `--input` file of a bare `{}` event is always passed, purely to match
 * [eu.bcosp.vrlintellij.playground.VRLPlaygroundRunner]'s already-proven-safe invocation shape
 * (`vector vrl` never has to fall back to reading an event from stdin, so there's no risk of it
 * blocking on stdin here). What that event actually contains doesn't affect which compile
 * diagnostics come back: VRL type-checks a program against the *static* type system - fields are
 * `any` unless narrowed within the script - not against real event data, so a fixed empty event is
 * exactly as good as a real one for this purpose.
 */
object VRLCompilerDiagnosticsRunner {

    private const val TIMEOUT_MS = 5_000

    fun run(vectorBinaryPath: String, program: String): String? {
        val programFile = Files.createTempFile("vrl-diagnostics-", ".vrl")
        val inputFile = Files.createTempFile("vrl-diagnostics-", ".json")
        try {
            Files.writeString(programFile, program, StandardCharsets.UTF_8)
            Files.writeString(inputFile, "{}", StandardCharsets.UTF_8)

            val commandLine = GeneralCommandLine(vectorBinaryPath)
                .withParameters(listOf("vrl", "--program", programFile.toString(), "--input", inputFile.toString()))
                .withCharset(StandardCharsets.UTF_8)
                // See VRLPlaygroundRunner: `vector vrl` always logs a startup line to stderr,
                // even on success, unless this is set.
                .withEnvironment("VECTOR_LOG", "off")

            val output = try {
                CapturingProcessHandler(commandLine).runProcess(TIMEOUT_MS)
            } catch (e: ProcessNotCreatedException) {
                return null
            }
            if (output.isTimeout) return null
            return output.stderr
        } finally {
            Files.deleteIfExists(programFile)
            Files.deleteIfExists(inputFile)
        }
    }
}
