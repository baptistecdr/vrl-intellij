package eu.bcosp.vrlintellij.playground

/**
 * Minimal, dependency-free pretty-printer for the single-line JSON the `vrl` CLI prints (VRL's
 * `Value::to_string()` output). Deliberately not a validating parser - it trusts the input is
 * well-formed JSON (which it always is, coming from the CLI) and just re-indents it.
 */
object VRLJsonFormatter {
    private const val INDENT = "  "

    fun prettyPrint(raw: String): String {
        val text = raw.trim()
        if (text.isEmpty()) return text

        val out = StringBuilder()
        var depth = 0
        var inString = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                inString -> {
                    out.append(c)
                    if (c == '\\' && i + 1 < text.length) {
                        out.append(text[i + 1])
                        i++
                    } else if (c == '"') {
                        inString = false
                    }
                }

                c == '"' -> {
                    inString = true
                    out.append(c)
                }

                c == '{' || c == '[' -> {
                    out.append(c)
                    val next = text.getOrNull(i + 1)
                    if (next != '}' && next != ']') {
                        depth++
                        out.append('\n').append(INDENT.repeat(depth))
                    }
                }

                c == '}' || c == ']' -> {
                    val prev = text.getOrNull(i - 1)
                    if (prev != '{' && prev != '[') {
                        depth--
                        out.append('\n').append(INDENT.repeat(depth))
                    }
                    out.append(c)
                }

                c == ',' -> out.append(c).append('\n').append(INDENT.repeat(depth))
                c == ':' -> out.append(": ")
                c.isWhitespace() -> Unit
                else -> out.append(c)
            }
            i++
        }
        return out.toString()
    }
}
