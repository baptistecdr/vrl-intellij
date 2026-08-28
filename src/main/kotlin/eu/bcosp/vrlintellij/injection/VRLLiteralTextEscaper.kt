package eu.bcosp.vrlintellij.injection

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

/**
 * Strips the delimiters common to every VRL string-shaped literal: an N-character prefix
 * (`"` for [VRLStringLiteralEscaper], `s'`/`r'` for [VRLRawLiteralEscaper]) and a single closing
 * quote - tolerating a missing closing quote so injection still works on a literal being typed.
 */
abstract class VRLLiteralTextEscaperBase(
    host: PsiLanguageInjectionHost,
    private val prefixLength: Int,
    private val quoteChar: Char,
) : LiteralTextEscaper<PsiLanguageInjectionHost>(host) {

    override fun getRelevantTextRange(): TextRange {
        val text = myHost.text
        val closed = text.length > prefixLength && text.last() == quoteChar
        val end = (if (closed) text.length - 1 else text.length).coerceAtLeast(prefixLength)
        return TextRange(prefixLength.coerceAtMost(end), end)
    }
}

/**
 * VRL's raw strings (`s'...'`) and regex literals (`r'...'`) apply no escape processing at all
 * (per the VRL reference), so decoding is a straight substring copy.
 */
class VRLRawLiteralEscaper(host: PsiLanguageInjectionHost) : VRLLiteralTextEscaperBase(host, prefixLength = 2, quoteChar = '\'') {

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        outChars.append(rangeInsideHost.subSequence(myHost.text))
        return true
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int =
        (rangeInsideHost.startOffset + offsetInDecoded).coerceIn(rangeInsideHost.startOffset, rangeInsideHost.endOffset)

    override fun isOneLine(): Boolean = false
}

/**
 * Decodes VRL's interpreted-string escapes (`\" \' \0 \\ \n \r \t \{` and `\u{...}`) so the
 * injected fragment sees real characters instead of escape sequences. Every decoded output
 * character is mapped back to the *start* offset of the host escape sequence that produced it,
 * which is the same "closest earlier offset" convention `LiteralTextEscaper` callers expect.
 */
class VRLStringLiteralEscaper(host: PsiLanguageInjectionHost) : VRLLiteralTextEscaperBase(host, prefixLength = 1, quoteChar = '"') {

    private var decodedToHostOffsets = IntArray(0)

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        val text = myHost.text
        var i = rangeInsideHost.startOffset
        val end = rangeInsideHost.endOffset
        val offsets = ArrayList<Int>(end - i + 1)
        while (i < end) {
            val start = i
            val before = outChars.length
            i = if (text[i] == '\\' && i + 1 < end) decodeEscape(text, i, end, outChars) else {
                outChars.append(text[i])
                i + 1
            }
            repeat(outChars.length - before) { offsets.add(start) }
        }
        offsets.add(end)
        decodedToHostOffsets = offsets.toIntArray()
        return true
    }

    private fun decodeEscape(text: String, escapeStart: Int, end: Int, outChars: StringBuilder): Int {
        val next = text[escapeStart + 1]
        return when (next) {
            '"', '\'', '\\', '{' -> {
                outChars.append(next)
                escapeStart + 2
            }

            '0' -> {
                outChars.append('\u0000')
                escapeStart + 2
            }

            'n' -> {
                outChars.append('\n')
                escapeStart + 2
            }

            'r' -> {
                outChars.append('\r')
                escapeStart + 2
            }

            't' -> {
                outChars.append('\t')
                escapeStart + 2
            }

            'u' -> decodeUnicodeEscape(text, escapeStart, end, outChars)
            else -> {
                outChars.append(next)
                escapeStart + 2
            }
        }
    }

    // \u{7FFF} - up to a 6-digit hex code point, per the VRL reference.
    private fun decodeUnicodeEscape(text: String, escapeStart: Int, end: Int, outChars: StringBuilder): Int {
        val braceStart = escapeStart + 2
        if (braceStart >= end || text[braceStart] != '{') {
            outChars.append('u')
            return escapeStart + 2
        }
        val braceEnd = text.indexOf('}', braceStart + 1)
        val codePoint = if (braceEnd in (braceStart + 1) until end) {
            text.substring(braceStart + 1, braceEnd).toIntOrNull(16)
        } else {
            null
        }
        if (codePoint == null || !Character.isValidCodePoint(codePoint)) {
            outChars.append('u')
            return escapeStart + 2
        }
        outChars.appendCodePoint(codePoint)
        return braceEnd + 1
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        if (decodedToHostOffsets.isEmpty()) return rangeInsideHost.startOffset
        val index = offsetInDecoded.coerceIn(0, decodedToHostOffsets.size - 1)
        return decodedToHostOffsets[index].coerceIn(rangeInsideHost.startOffset, rangeInsideHost.endOffset)
    }

    override fun isOneLine(): Boolean = true
}
