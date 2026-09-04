package eu.bcosp.vrlintellij.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import eu.bcosp.vrlintellij.functions.VRLFunction
import eu.bcosp.vrlintellij.functions.allFunctions
import eu.bcosp.vrlintellij.psi.VRLElementTypes

class VRLDocumentationProvider : AbstractDocumentationProvider() {

    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int
    ): PsiElement? {
        return contextElement?.takeIf { it.isFunctionCallToken() }
    }

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        if (!element.isFunctionCallToken()) return null
        val function = allFunctions[element.text] ?: return null
        return function.toHtml()
    }

    override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?): String? {
        if (!element.isFunctionCallToken()) return null
        val function = allFunctions[element.text] ?: return null
        return "${function.name}(${function.signature()}) -> ${function.returnTypes.joinToString("|")}"
    }

    // Every VRL function lives on one combined reference page, addressed by an anchor matching
    // its exact name (e.g. https://vector.dev/docs/reference/vrl/functions/#parse_json) - this is
    // what wires up the Quick Documentation popup's "open in browser" icon and the platform's
    // External Documentation action (Shift+F1) for free, with no custom action of our own needed.
    override fun getUrlFor(element: PsiElement, originalElement: PsiElement?): List<String>? {
        if (!element.isFunctionCallToken()) return null
        val function = allFunctions[element.text] ?: return null
        return listOf("https://vector.dev/docs/reference/vrl/functions/#${function.name}")
    }

    private fun PsiElement.isFunctionCallToken(): Boolean {
        return node.elementType == VRLElementTypes.FUNCTION_CALL
    }

    private fun VRLFunction.signature(): String =
        arguments.joinToString(", ") { arg ->
            val optional = if (arg.isRequired) "" else "?"
            "${arg.name}$optional: ${arg.types.joinToString("|")}"
        }

    private fun VRLFunction.toHtml(): String = buildString {
        append(DocumentationMarkup.DEFINITION_START)
        append(StringUtil.escapeXmlEntities("$name(${signature()}) -> ${returnTypes.joinToString("|")}"))
        append(DocumentationMarkup.DEFINITION_END)

        append(DocumentationMarkup.CONTENT_START)
        append(StringUtil.escapeXmlEntities(description))
        append(DocumentationMarkup.CONTENT_END)

        if (arguments.isNotEmpty()) {
            append(DocumentationMarkup.SECTIONS_START)
            for (arg in arguments) {
                append(DocumentationMarkup.SECTION_HEADER_START)
                append(StringUtil.escapeXmlEntities(arg.name))
                if (!arg.isRequired) append(" <i>(optional)</i>")
                append(DocumentationMarkup.SECTION_SEPARATOR)
                append(StringUtil.escapeXmlEntities(arg.description))
                append(DocumentationMarkup.SECTION_END)
            }
            append(DocumentationMarkup.SECTIONS_END)
        }

        val badges = buildList {
            if (isPure) add("pure")
            if (isFallible) add("fallible")
        }
        if (badges.isNotEmpty()) {
            append("<p><i>${badges.joinToString(", ")}</i></p>")
        }
    }
}
