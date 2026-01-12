package eu.bcosp.vrlintellij.colors

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.openapi.util.NlsContexts
import eu.bcosp.vrlintellij.VRL
import eu.bcosp.vrlintellij.highlighting.VRLHighlighter
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

class VRLColorSettingsPage: ColorSettingsPage {
    private val attributes = VRLColor.entries.map { it.attributesDescriptor }.toTypedArray()

    private val annotatorTags = VRLColor.entries.associateBy({ it.name }, { it.textAttributesKey })

    override fun getIcon(): Icon? {
        return null
    }

    override fun getHighlighter(): SyntaxHighlighter {
        return VRLHighlighter()
    }

    override fun getDemoText(): @NonNls String {
        return """
            # Remove some fields
            del(.foo)
            
            # Add a timestamp
            .timestamp = now()
            
            # Parse HTTP status code into local variable
            http_status_code = parse_int!(.http_status)
            del(.http_status)
            
            # Add status
            if http_status_code >= 200 && http_status_code <= 299 {
                .status = "success"
            } else {
                .status = "error"
            }
            "Hello, world! 🌎"
            "Hello, world! \u1F30E"
            "Hello, \
             world!"
            "Hello, {{ planet }}!"
            s'Hello, world!'
            s'{ "foo": "bar" }'
            t'2021-02-11T10:32:50.553955473Z'
            split("hello, world!", <NAMED_ARGUMENTS>pattern</NAMED_ARGUMENTS>: ", ")
            tally = {}
            for_each(array!(.tags)) -> |_index, value| {
                # Get the current tally for the `value`, or
                # set to `0`.
                count = int(get!(tally, [value])) ?? 0

                # Increment the tally for the value by `1`.
                tally = set!(tally, [value], count + 1)
            }
        """.trimIndent()
    }

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> {
        return annotatorTags
    }

    override fun getAttributeDescriptors(): Array<out AttributesDescriptor> {
        return attributes
    }

    override fun getColorDescriptors(): Array<out ColorDescriptor> {
        return ColorDescriptor.EMPTY_ARRAY
    }

    override fun getDisplayName(): @NlsContexts.ConfigurableName String {
        return VRL.id
    }
}
