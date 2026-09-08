package eu.bcosp.vrlintellij.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

class VRLInvalidTimestampInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType != VRLElementTypes.TIMESTAMP) return
                val content = element.text.removePrefix("t'").removeSuffix("'")

                if (!isValidVrlTimestamp(content)) {
                    holder.registerProblem(
                        element,
                        "Invalid timestamp literal: not valid RFC 3339 (expected e.g. '2021-02-11T10:32:50.553955473Z')",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    )
                }
            }
        }
    }

    private fun isValidVrlTimestamp(content: String): Boolean {
        val g = RELAXED_RFC3339.matchEntire(content)?.groupValues ?: return false
        val year = g[1]
        val month = g[2]
        val day = g[3]
        val hour = g[4]
        val minute = g[5]
        val second = g[6]
        val fraction = g[7]
        val zulu = g[8]
        val offsetSign = g[9]
        val offsetHour = g[10]
        val offsetMinute = g[11]

        // java.time's OffsetDateTime has no representation for a leap second, so :60 (the one
        // second value VRL's chrono-based parser accepts beyond the normal 0-59 range) is
        // normalized to :59 purely so the rest of the literal (year/month/day/hour/minute/offset)
        // still gets validated - :61 and above are left alone and rejected below like any other
        // out-of-range field.
        val normalized = buildString {
            append(year.padStart(4, '0')).append('-')
            append(month.padStart(2, '0')).append('-')
            append(day.padStart(2, '0')).append('T')
            append(hour.padStart(2, '0')).append(':')
            append(minute.padStart(2, '0')).append(':')
            append(if (second == "60") "59" else second.padStart(2, '0'))
            if (fraction.isNotEmpty()) append('.').append(fraction)
            if (zulu.isNotEmpty()) {
                append('Z')
            } else {
                append(offsetSign).append(offsetHour.padStart(2, '0')).append(':').append(offsetMinute.padStart(2, '0'))
            }
        }
        return isParsable(normalized)
    }

    private fun isParsable(content: String): Boolean = try {
        OffsetDateTime.parse(content)
        true
    } catch (e: DateTimeParseException) {
        false
    }

    companion object {
        // Mirrors the grammar VRL's own `t'...'` literal actually accepts - `Timestamp(v) =>
        // v.parse()` in vrl's compiler.rs parses with chrono's `DateTime<Utc>: FromStr`, which is
        // documented as "a relaxed form of RFC 3339" (chrono's parse_rfc3339_relaxed): 'T', 't', or
        // ' ' as the date/time separator, 'Z'/'z' or an explicit +-HH:MM/+-HHMM offset (colon
        // optional), unpadded single-digit fields, and a lone leap second (":60"). Unlike the
        // plain RFC 3339 shape VRL's docs happen to show in examples, none of that is
        // java.time-lenient-by-default - java.time's ISO_OFFSET_DATE_TIME is the stricter one here,
        // which is why every field is normalized before being handed to it.
        private val RELAXED_RFC3339 = Regex(
            """(\d{1,4})-(\d{1,2})-(\d{1,2})[Tt ](\d{1,2}):(\d{1,2}):(\d{1,2})(?:\.(\d+))?(?:([Zz])|([+-])(\d{2}):?(\d{2}))""",
        )
    }
}
