package eu.bcosp.vrlintellij.injection

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import eu.bcosp.vrlintellij.VRL

class VRLTomlConfigInjectorTest : BasePlatformTestCase() {

    private fun injectedElementAt(text: String, needle: String) = run {
        myFixture.configureByText("vector.toml", text)
        val offset = myFixture.file.text.indexOf(needle)
        InjectedLanguageManager.getInstance(myFixture.project).findInjectedElementAt(myFixture.file, offset)
    }

    fun testInjectsIntoRemapSource() {
        val injected = injectedElementAt(
            "[transforms.parse]\ntype = \"remap\"\nsource = \".foo = 1\"\n",
            ".foo",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testDoesNotInjectSourceOfNonRemapTransform() {
        val injected = injectedElementAt(
            "[transforms.parse]\ntype = \"lua\"\nsource = \"print('hi')\"\n",
            "print",
        )
        assertNull(injected)
    }

    fun testInjectsIntoPlainStringCondition() {
        val injected = injectedElementAt(
            "[transforms.drop]\ntype = \"filter\"\ncondition = \".status == 200\"\n",
            ".status",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testDoesNotInjectTableFormCondition() {
        val injected = injectedElementAt(
            "[transforms.drop]\ntype = \"filter\"\ncondition = { type = \"datadog_search\", source = \"*stack\" }\n",
            "*stack",
        )
        assertNull(injected)
    }

    fun testDoesNotInjectUnrelatedKeys() {
        val injected = injectedElementAt(
            "[transforms.parse]\ntype = \"remap\"\ntimezone = \"UTC\"\n",
            "UTC",
        )
        assertNull(injected)
    }

    fun testInjectsIntoVrlCodecDecodingSourceTableHeaderForm() {
        // The exact shape from https://vector.dev/docs/reference/configuration/sources/http_server/#decoding.vrl.source
        val injected = injectedElementAt(
            "[sources.my_source_id]\ntype = \"http_server\"\naddress = \"0.0.0.0:80\"\n\n" +
                "[sources.my_source_id.decoding]\ncodec = \"vrl\"\n\n" +
                "[sources.my_source_id.decoding.vrl]\nsource = \". |= parse_json!\"\ntimezone = \"America/New_York\"\n",
            "parse_json",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testInjectsIntoVrlCodecSourceInlineTableForm() {
        val injected = injectedElementAt(
            "[sources.my_source_id.decoding]\ncodec = \"vrl\"\nvrl = { source = \". |= parse_json!\" }\n",
            "parse_json",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testInjectsIntoVrlCodecSourceDottedKeyForm() {
        val injected = injectedElementAt(
            "[sources.my_source_id]\ndecoding.codec = \"vrl\"\ndecoding.vrl.source = \". |= parse_json!\"\n",
            "parse_json",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testDoesNotInjectSourceOfNonVrlCodec() {
        val injected = injectedElementAt(
            "[sources.my_source_id.decoding]\ncodec = \"json\"\n\n" +
                "[sources.my_source_id.decoding.json]\nsource = \"not vrl\"\n",
            "not vrl",
        )
        assertNull(injected)
    }
}
