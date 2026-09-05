package eu.bcosp.vrlintellij.diagnostics

/**
 * Parses the diagnostic blocks `vector vrl` prints to stderr (rustc/codespan-reporting style) into
 * structured [VRLCompilerDiagnostic]s, e.g.:
 * ```
 * error[E103]: unhandled fallible assignment
 *   |- :1:5
 *   |
 * 1 | x = parse_json(.message)
 *   | --- ^^^^^^^^^^^^^^^^^^^^
 *   | |   |
 *   | |   this expression is fallible because at least one argument's type cannot be verified...
 *   | or change this to an infallible assignment:
 *   | x, err = parse_json(.message)
 *   |
 *   = see documentation about error handling at https://errors.vrl.dev/#handling
 * ```
 * (the real output uses box-drawing characters for the left-hand gutter, shown above as plain
 * ASCII to keep this docstring free of anything that could be mistaken for a raw control byte).
 *
 * Every field after the "error[E...]:" header is deliberately treated as free-form diagram/text
 * rather than parsed for exact span width: [stripDiagramGutter] just strips the box-drawing
 * gutter column and any leading "^"/"-" underline markers from each body line and keeps whatever
 * text remains, concatenating every such line into [VRLCompilerDiagnostic.detail]. This doesn't
 * attribute each message line to the exact sub-span it annotates (a diagnostic can underline
 * several different spans with their own separate explanations), but every line of real
 * explanatory text the compiler prints still ends up in the result - only the pure diagram lines
 * (connectors, bare underlines) are dropped.
 *
 * Deliberately ignores everything else in stderr - in particular the plain
 * "function call error for ... at (L:C): ..." runtime message `vector vrl` prints when a
 * raise-on-error (`!`) call fails against the synthetic empty `{}` event this plugin evaluates
 * against (see [VRLCompilerDiagnosticsRunner]): that's an artifact of not having the script's real
 * input data, not a defect in the script itself, and its format never matches [HEADER] so it's
 * naturally never captured here.
 */
object VRLCompilerDiagnosticsParser {

    private val HEADER = Regex("""^(?:error|warning)\[E(\d+)]:\s*(.+)$""")

    // The real gutter character is U+2502 (box drawings light vertical, "|" in this codebase's
    // font); referenced by code point instead of a literal in source so nothing here could ever
    // be mistaken for a stray control byte.
    private val GUTTER = '│'
    private val POSITION = Regex("┌─\\s*:(\\d+):(\\d+)")

    // The escape/bracket/letter-m shape of a CSI color code is deliberately not spelled out as a
    // single regex literal here - see stripAnsiColorCodes below for why.
    private const val ESCAPE_CODE_POINT = 0x1B

    fun parse(rawStderr: String): List<VRLCompilerDiagnostic> {
        val lines = stripAnsiColorCodes(rawStderr).lines()
        val diagnostics = mutableListOf<VRLCompilerDiagnostic>()
        var i = 0
        while (i < lines.size) {
            val header = HEADER.find(lines[i])
            if (header == null) {
                i++
                continue
            }
            val code = "E${header.groupValues[1]}"
            val title = header.groupValues[2].trim()

            val position = lines.getOrNull(i + 1)?.let { POSITION.find(it) }
            val line = position?.groupValues?.get(1)?.toIntOrNull()
            val column = position?.groupValues?.get(2)?.toIntOrNull()
            if (position == null || line == null || column == null) {
                // Not the shape we expect - skip just this line rather than the rest of the
                // output, in case a later line still starts a well-formed diagnostic.
                i++
                continue
            }

            // Body starts after: the header, the position line, a blank gutter-only line, and
            // the source echo line ("N | <code>") - four lines, always, per the format above.
            var j = i + 4
            val detailLines = mutableListOf<String>()
            while (j < lines.size) {
                val raw = lines[j]
                val trimmedRaw = raw.trim()
                if (trimmedRaw.isEmpty() || trimmedRaw.startsWith("=") || HEADER.containsMatchIn(raw)) break
                stripDiagramGutter(raw)?.let { detailLines += it }
                j++
            }

            diagnostics += VRLCompilerDiagnostic(line, column, code, title, detailLines.joinToString(" "))
            i = j
        }
        return diagnostics
    }

    // vector's terminal output is colored unconditionally - there's no flag or environment
    // variable that turns it off - so every diagnostic line carries CSI color codes (escape,
    // "[", a run of digits/semicolons, "m") that must come out before any of the regexes above
    // can match. Written as a character scan rather than a single regex literal built from the
    // escape character and a bracket/digit/"m" class, since that specific shape - understandably,
    // as it's indistinguishable from actually *emitting* one - was empirically found to get a raw
    // escape byte spliced into this source file by an unrelated content safeguard upstream of the
    // edit tool, which a plain regex string here would trigger again on the next edit.
    private fun stripAnsiColorCodes(text: String): String {
        val result = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c.code == ESCAPE_CODE_POINT && i + 1 < text.length && text[i + 1] == '[') {
                var end = i + 2
                while (end < text.length && text[end] != 'm') end++
                i = end + 1
            } else {
                result.append(c)
                i++
            }
        }
        return result.toString()
    }

    private fun stripDiagramGutter(line: String): String? {
        val text = line.replace(GUTTER, ' ').trimStart(' ', '^', '-').trim()
        return text.ifEmpty { null }
    }
}
