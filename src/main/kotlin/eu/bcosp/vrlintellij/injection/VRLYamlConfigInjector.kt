package eu.bcosp.vrlintellij.injection

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import eu.bcosp.vrlintellij.VRL
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

/**
 * YAML counterpart to [VRLTomlConfigInjector] - same config keys
 * (https://vector.dev/docs/reference/configuration/), same scoping rationale (including both the
 * `vrl`-codec `decoding.vrl.source`/`encoding.vrl.source` shape, e.g.
 * https://vector.dev/docs/reference/configuration/sources/http_server/#decoding.vrl.source, and
 * the fully-specified condition's `type: vrl` + `source:` - which is the exact shape Vector's own
 * `deserialize_anycondition_vrl` test in src/conditions/mod.rs is written in), just matched
 * against the bundled YAML plugin's PSI instead of TOML's. Declared as an optional dependency
 * (`org.jetbrains.plugins.yaml`, see plugin.xml's `vrl-yaml.xml` config file).
 */
class VRLYamlConfigInjector : MultiHostInjector {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val keyValue = context as? YAMLKeyValue ?: return
        val host = keyValue.value as? YAMLScalar ?: return
        if (!host.isValidHost) return

        val isVrl = when (keyValue.keyText) {
            "source" -> siblingType(keyValue) in VRL_SOURCE_TYPES || isNestedUnderVrlCodec(keyValue)
            "condition" -> true
            else -> false
        }
        if (!isVrl) return

        val range = host.createLiteralTextEscaper().relevantTextRange
        registrar.startInjecting(VRL)
            .addPlace(null, null, host, range)
            .doneInjecting()
    }

    // Unlike TOML, YAML has no dotted-key form - a condition's `type`/`source` are always both
    // entries of the same nested mapping - so the enclosing mapping is already the right scope,
    // with no key-prefix matching needed (contrast [VRLTomlConfigInjector.siblingType]).
    private fun siblingType(keyValue: YAMLKeyValue): String? {
        val mapping = keyValue.parentMapping ?: return null
        return (mapping.getKeyValueByKey("type")?.value as? YAMLScalar)?.textValue
    }

    // `decoding:\n  codec: vrl\n  vrl:\n    source: ...` (or the flow-mapping `vrl: { source: ... }`
    // equivalent) - the enclosing mapping's own key must literally be named `vrl`.
    private fun isNestedUnderVrlCodec(keyValue: YAMLKeyValue): Boolean {
        val mapping = keyValue.parentMapping ?: return false
        val containingKeyValue = mapping.parent as? YAMLKeyValue ?: return false
        return containingKeyValue.keyText == "vrl"
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> = listOf(YAMLKeyValue::class.java)

    companion object {
        // See VRLTomlConfigInjector.VRL_SOURCE_TYPES - a remap transform's program, or a
        // fully-specified VRL condition's boolean expression.
        private val VRL_SOURCE_TYPES = setOf("remap", "vrl")
    }
}
