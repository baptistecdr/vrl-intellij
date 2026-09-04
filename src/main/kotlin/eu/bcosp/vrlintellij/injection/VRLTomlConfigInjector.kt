package eu.bcosp.vrlintellij.injection

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import eu.bcosp.vrlintellij.VRL
import org.toml.lang.psi.TomlInlineTable
import org.toml.lang.psi.TomlKey
import org.toml.lang.psi.TomlKeyValue
import org.toml.lang.psi.TomlKeyValueOwner
import org.toml.lang.psi.TomlLiteral
import org.toml.lang.psi.TomlTable
import org.toml.lang.psi.ext.TomlLiteralKind
import org.toml.lang.psi.ext.kind

/**
 * Real-world VRL almost always lives inline inside a Vector pipeline config, not a standalone
 * `.vrl` file - so this injects VRL directly into the config keys that hold a VRL program in
 * Vector's TOML format (https://vector.dev/docs/reference/configuration/):
 * - `source` under a `remap` transform (`type = "remap"`), e.g.
 *   `[transforms.parse]\ntype = "remap"\nsource = ".foo = 1"`.
 * - `condition`, whenever given as a plain string - per Vector's own docs this always defaults to
 *   a VRL boolean expression (used by `filter`/`route`/swimlane conditions). The table form
 *   (`condition = { type = "datadog_search", source = "..." }`) is deliberately left alone since
 *   distinguishing its non-VRL condition types isn't worth the added complexity here.
 * - `source` under a `vrl` codec table (`decoding.vrl.source` / `encoding.vrl.source`, e.g.
 *   https://vector.dev/docs/reference/configuration/sources/http_server/#decoding.vrl.source) -
 *   any source's `decoding` or any sink's `encoding` can be set to `codec = "vrl"`, which then
 *   takes its own nested `vrl.source`. This is a distinct shape from the transform case: the
 *   "this is VRL" signal is an ancestor table/key literally named `vrl`, not a sibling `type`.
 *
 * Declared as an optional dependency (`org.toml.lang`, see plugin.xml's `vrl-toml.xml` config
 * file) since not every IDE this plugin runs in bundles TOML support.
 */
class VRLTomlConfigInjector : MultiHostInjector {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val keyValue = context as? TomlKeyValue ?: return
        val host = keyValue.value as? TomlLiteral ?: return
        if (host.kind !is TomlLiteralKind.String) return
        if (!host.isValidHost) return

        val isVrl = when (lastSegmentName(keyValue.key)) {
            "source" -> siblingTypeIsRemap(keyValue) || isNestedUnderVrlCodec(keyValue)
            "condition" -> true
            else -> false
        }
        if (!isVrl) return

        val range = (host as PsiLanguageInjectionHost).createLiteralTextEscaper().relevantTextRange
        registrar.startInjecting(VRL)
            .addPlace(null, null, host, range)
            .doneInjecting()
    }

    private fun siblingTypeIsRemap(keyValue: TomlKeyValue): Boolean {
        val owner = keyValue.parent as? TomlKeyValueOwner ?: return false
        val typeLiteral = owner.entries.firstOrNull { lastSegmentName(it.key) == "type" }?.value as? TomlLiteral ?: return false
        val kind = typeLiteral.kind as? TomlLiteralKind.String ?: return false
        return kind.value == "remap"
    }

    private fun isNestedUnderVrlCodec(keyValue: TomlKeyValue): Boolean {
        // Dotted-key form: `decoding.vrl.source = "..."` - the key itself ends in `vrl.source`.
        val segments = keyValue.key.segments
        if (segments.size >= 2 && segments[segments.size - 2].name == "vrl") return true

        val owner = keyValue.parent
        // Table-header form: `[sources.x.decoding.vrl]` with `source = "..."` as a direct entry.
        if (owner is TomlTable) {
            val headerSegments = owner.header.key?.segments
            if (headerSegments?.lastOrNull()?.name == "vrl") return true
        }
        // Inline-table form: `vrl = { source = "...", ... }`.
        if (owner is TomlInlineTable) {
            val containingKeyValue = owner.parent as? TomlKeyValue
            if (containingKeyValue != null && lastSegmentName(containingKeyValue.key) == "vrl") return true
        }
        return false
    }

    // [TomlKey.name] (from `org.toml.lang.psi.ext`) only resolves for a single-segment key and is
    // null for a dotted one (`decoding.vrl.source`) - Vector configs use both styles for the same
    // field, so every lookup here goes through the key's last segment instead, which is defined
    // either way.
    private fun lastSegmentName(key: TomlKey): String? = key.segments.lastOrNull()?.name

    override fun elementsToInjectIn(): List<Class<out PsiElement>> = listOf(TomlKeyValue::class.java)
}
