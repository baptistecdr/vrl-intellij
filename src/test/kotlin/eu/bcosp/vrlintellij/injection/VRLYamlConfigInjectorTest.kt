package eu.bcosp.vrlintellij.injection

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import eu.bcosp.vrlintellij.VRL

class VRLYamlConfigInjectorTest : BasePlatformTestCase() {

    private fun injectedElementAt(text: String, needle: String) = run {
        myFixture.configureByText("vector.yaml", text)
        val offset = myFixture.file.text.indexOf(needle)
        InjectedLanguageManager.getInstance(myFixture.project).findInjectedElementAt(myFixture.file, offset)
    }

    fun testInjectsIntoRemapSource() {
        val injected = injectedElementAt(
            "transforms:\n  parse:\n    type: remap\n    source: |\n      .foo = 1\n",
            ".foo",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testDoesNotInjectSourceOfNonRemapTransform() {
        val injected = injectedElementAt(
            "transforms:\n  parse:\n    type: lua\n    source: |\n      print('hi')\n",
            "print",
        )
        assertNull(injected)
    }

    fun testInjectsIntoPlainStringCondition() {
        val injected = injectedElementAt(
            "transforms:\n  drop:\n    type: filter\n    condition: \".status == 200\"\n",
            ".status",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testDoesNotInjectMappingFormCondition() {
        val injected = injectedElementAt(
            "transforms:\n  drop:\n    type: filter\n    condition:\n      type: datadog_search\n      source: \"*stack\"\n",
            "*stack",
        )
        assertNull(injected)
    }

    fun testDoesNotInjectUnrelatedKeys() {
        val injected = injectedElementAt(
            "transforms:\n  parse:\n    type: remap\n    timezone: UTC\n",
            "UTC",
        )
        assertNull(injected)
    }

    fun testInjectsIntoVrlCodecDecodingSourceBlockMappingForm() {
        // The exact shape from https://vector.dev/docs/reference/configuration/sources/http_server/#decoding.vrl.source
        val injected = injectedElementAt(
            "sources:\n  my_source_id:\n    type: http_server\n    address: 0.0.0.0:80\n" +
                "    decoding:\n      codec: vrl\n      vrl:\n        source: \". |= parse_json!\"\n" +
                "        timezone: America/New_York\n",
            "parse_json",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testInjectsIntoVrlCodecSourceFlowMappingForm() {
        val injected = injectedElementAt(
            "decoding:\n  codec: vrl\n  vrl: { source: \". |= parse_json!\" }\n",
            "parse_json",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testDoesNotInjectSourceOfNonVrlCodec() {
        val injected = injectedElementAt(
            "decoding:\n  codec: json\n  json:\n    source: \"not vrl\"\n",
            "not vrl",
        )
        assertNull(injected)
    }
}
