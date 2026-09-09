package eu.bcosp.vrlintellij.intentions

/**
 * Converts between the contents of VRL's two plain string forms, refusing whenever the meaning
 * couldn't survive the trip.
 *
 * The two forms are not interchangeable, and the differences were checked against
 * `vector vrl` 0.58.0 rather than taken from the reference:
 *
 * - `"..."` processes the escapes `\" \' \0 \\ \n \r \t \{` and `\u{...}` (the same set
 *   [eu.bcosp.vrlintellij.injection.VRLStringLiteralEscaper] decodes for language injection);
 *   anything else, such as `"a\qb"`, is rejected outright as compiler error 209.
 * - `"..."` interpolates `{{ ... }}` while `s'...'` does not: with `x = "V"`, `"got {{x}}"`
 *   evaluates to `got V` but `s'got {{x}}'` stays `got {{x}}`. A single `{`, a single `}`, and
 *   even `}}` are literal - only a doubled `{{` starts an interpolation, and `\{{` escapes it.
 * - A raw string cannot contain a single quote *at all*. Both `s'a'b'` and the doubled
 *   `s'a''b'` are compiler error 203, even though this plugin's own lexer rule spells
 *   RAW_STRING as `s'([^']|'')*'` - the lexer is more permissive than the language.
 */
internal object VRLStringLiteralConversion {

    // Written as Char(0) rather than a unicode escape so the source stays plain ASCII.
    private val NUL = Char(0)

    /**
     * The literal text a raw string should hold to mean the same as this interpreted string's
     * contents, or null when no raw string can: the value contains a `'`, contains a control
     * character that would have to be written literally, or the source interpolates.
     */
    fun interpretedToRaw(contents: String): String? {
        if (containsInterpolation(contents)) return null
        val decoded = decodeEscapes(contents) ?: return null
        if (decoded.any { it == '\'' || it.isISOControl() }) return null
        return decoded
    }

    /**
     * The literal text an interpreted string should hold to mean the same as this raw string's
     * contents. Always possible: every raw character has an escape, and a raw string can't
     * contain the one character (`'`) that has no place in the interpreted form either.
     */
    fun rawToInterpreted(contents: String): String {
        val out = StringBuilder(contents.length)
        var i = 0
        while (i < contents.length) {
            val c = contents[i]
            when {
                // Escaping the first brace is enough to stop the pair being read as an
                // interpolation - `"got \{{x}}"` evaluates to the literal `got {{x}}`.
                c == '{' && i + 1 < contents.length && contents[i + 1] == '{' -> out.append("\\{{").also { i += 2 }
                c == '\\' -> out.append("\\\\").also { i++ }
                c == '"' -> out.append("\\\"").also { i++ }
                c == '\n' -> out.append("\\n").also { i++ }
                c == '\r' -> out.append("\\r").also { i++ }
                c == '\t' -> out.append("\\t").also { i++ }
                c == NUL -> out.append("\\0").also { i++ }
                else -> out.append(c).also { i++ }
            }
        }
        return out.toString()
    }

    /** True if an unescaped `{{` makes this interpreted string's value depend on runtime state. */
    private fun containsInterpolation(contents: String): Boolean {
        var i = 0
        while (i < contents.length) {
            val c = contents[i]
            if (c == '\\') {
                i += 2
                continue
            }
            if (c == '{' && i + 1 < contents.length && contents[i + 1] == '{') return true
            i++
        }
        return false
    }

    /** The value of an interpreted string's contents, or null if it holds an escape VRL rejects. */
    private fun decodeEscapes(contents: String): String? {
        val out = StringBuilder(contents.length)
        var i = 0
        while (i < contents.length) {
            val c = contents[i]
            if (c != '\\') {
                out.append(c)
                i++
                continue
            }
            if (i + 1 >= contents.length) return null
            when (val escaped = contents[i + 1]) {
                '"', '\'', '\\', '{' -> {
                    out.append(escaped)
                    i += 2
                }

                '0' -> {
                    out.append(NUL)
                    i += 2
                }

                'n' -> {
                    out.append('\n')
                    i += 2
                }

                'r' -> {
                    out.append('\r')
                    i += 2
                }

                't' -> {
                    out.append('\t')
                    i += 2
                }

                'u' -> {
                    val end = decodeUnicodeEscape(contents, i, out) ?: return null
                    i = end
                }

                // Not an escape VRL accepts - the source is already error 209, so refuse rather
                // than guess at what it was meant to say.
                else -> return null
            }
        }
        return out.toString()
    }

    private fun decodeUnicodeEscape(contents: String, escapeStart: Int, out: StringBuilder): Int? {
        val braceStart = escapeStart + 2
        if (braceStart >= contents.length || contents[braceStart] != '{') return null
        val braceEnd = contents.indexOf('}', braceStart + 1)
        if (braceEnd < 0) return null
        val codePoint = contents.substring(braceStart + 1, braceEnd).toIntOrNull(16) ?: return null
        if (!Character.isValidCodePoint(codePoint)) return null
        out.appendCodePoint(codePoint)
        return braceEnd + 1
    }
}
