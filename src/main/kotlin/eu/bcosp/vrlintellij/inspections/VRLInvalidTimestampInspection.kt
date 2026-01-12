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

                if (!RFC3339_FORMAT.matches(content) || !isParsable(content)) {
                    holder.registerProblem(
                        element,
                        "Invalid timestamp literal: not valid RFC 3339 (expected e.g. '2021-02-11T10:32:50.553955473Z')",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    )
                }
            }
        }
    }

    private fun isParsable(content: String): Boolean = try {
        OffsetDateTime.parse(content)
        true
    } catch (e: DateTimeParseException) {
        false
    }

    companion object {
        // Uppercase 'T'/'Z' and an explicit offset, matching VRL's documented examples exactly -
        // java.time's own RFC-3339 parser is more lenient (accepts lowercase 't'/'z') than VRL is.
        private val RFC3339_FORMAT =
            Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?(Z|[+-]\d{2}:\d{2})""")
    }
}
