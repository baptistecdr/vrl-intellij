package eu.bcosp.vrlintellij.diagnostics

/**
 * One diagnostic block from `vector vrl`'s own compiler output, e.g.:
 * ```
 * error[E103]: unhandled fallible assignment
 *   ┌─ :1:5
 *   │
 * 1 │ x = parse_json(.message)
 *   │ --- ^^^^^^^^^^^^^^^^^^^^
 *   ...
 * ```
 * [line]/[column] are 1-indexed, matching how the compiler itself reports them.
 */
data class VRLCompilerDiagnostic(
    val line: Int,
    val column: Int,
    val code: String,
    val title: String,
    val detail: String,
)
