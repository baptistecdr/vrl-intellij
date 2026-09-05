package eu.bcosp.vrlintellij.playground

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessNotCreatedException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

sealed class VRLPlaygroundResult {
    data class Success(val expressionResult: String, val mutatedEvent: String) : VRLPlaygroundResult()
    data class Failure(val message: String) : VRLPlaygroundResult()
}

/**
 * Runs a VRL program against a sample event via Vector's own `vector vrl` subcommand
 * (https://vector.dev/docs/reference/cli/#vrl), which wraps the same VRL CLI
 * (https://github.com/vectordotdev/vrl) argument parser and flags. Vector is what users actually
 * have installed - the standalone `vrl` crate isn't a normal part of anyone's toolchain - so the
 * configured path always points at the `vector` binary, and this always runs it with a leading
 * `vrl` subcommand argument. Two invocations are made per run: one plain (the final expression's
 * value - what a single `parse_json!(.)` line evaluates to) and one with `--print-object` (the
 * event after the script ran against it, which is what most `.foo = ...`-shaped remap scripts
 * actually mutate). Both are cheap, so paying for two processes on an explicit, user-triggered
 * "Run" is preferable to guessing which one the user wants.
 */
object VRLPlaygroundRunner {

    private const val TIMEOUT_MS = 10_000

    fun run(vectorBinaryPath: String, program: String, sampleEvent: String): VRLPlaygroundResult {
        val programFile = Files.createTempFile("vrl-playground-", ".vrl")
        val inputFile = Files.createTempFile("vrl-playground-", ".json")
        try {
            Files.writeString(programFile, program, StandardCharsets.UTF_8)
            Files.writeString(inputFile, sampleEvent, StandardCharsets.UTF_8)

            val expressionRun = execute(vectorBinaryPath, programFile, inputFile, printObject = false)
            val expressionText = when (expressionRun) {
                is VRLPlaygroundResult.Failure -> return expressionRun
                is VRLPlaygroundResult.Success -> expressionRun.expressionResult
            }

            val objectRun = execute(vectorBinaryPath, programFile, inputFile, printObject = true)
            val mutatedEvent = when (objectRun) {
                is VRLPlaygroundResult.Failure -> return objectRun
                is VRLPlaygroundResult.Success -> objectRun.mutatedEvent
            }

            return VRLPlaygroundResult.Success(expressionText, mutatedEvent)
        } finally {
            Files.deleteIfExists(programFile)
            Files.deleteIfExists(inputFile)
        }
    }

    private fun execute(
        vectorBinaryPath: String,
        programFile: Path,
        inputFile: Path,
        printObject: Boolean,
    ): VRLPlaygroundResult {
        val commandLine = GeneralCommandLine(vectorBinaryPath)
            .withParameters(
                buildList {
                    add("vrl")
                    add("--program"); add(programFile.toString())
                    add("--input"); add(inputFile.toString())
                    if (printObject) add("--print-object")
                },
            )
            .withCharset(StandardCharsets.UTF_8)
            // Unlike the standalone `vrl` binary, `vector vrl` always logs an
            // "INFO vector::app: Log level is enabled." line to stderr on startup - even on a
            // successful run - which would otherwise make every run look like a failure (stderr
            // is the only reliable error signal here, see below). `--quiet` doesn't touch it since
            // it comes from Vector's global tracing subscriber init, not the `vrl` subcommand's
            // own logging; VECTOR_LOG=off does.
            .withEnvironment("VECTOR_LOG", "off")

        val output = try {
            CapturingProcessHandler(commandLine).runProcess(TIMEOUT_MS)
        } catch (e: ProcessNotCreatedException) {
            return VRLPlaygroundResult.Failure(
                "Couldn't run \"$vectorBinaryPath\": ${e.message}\n\n" +
                    "Set the correct path to the vector executable in Settings/Preferences | Tools | VRL Playground.",
            )
        }

        if (output.isTimeout) {
            return VRLPlaygroundResult.Failure("\"$vectorBinaryPath\" timed out after ${TIMEOUT_MS}ms.")
        }

        val stderr = stripAnsi(output.stderr).trim()
        if (output.exitCode != 0 || stderr.isNotEmpty()) {
            return VRLPlaygroundResult.Failure(stderr.ifEmpty { "\"$vectorBinaryPath\" exited with code ${output.exitCode}." })
        }

        val stdout = output.stdout.trim()
        return if (printObject) {
            VRLPlaygroundResult.Success(expressionResult = "", mutatedEvent = stdout)
        } else {
            VRLPlaygroundResult.Success(expressionResult = stdout, mutatedEvent = "")
        }
    }

    private fun stripAnsi(text: String): String = text.replace(Regex("\\[[;\\d]*m"), "")
}
